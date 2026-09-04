package bo.edu.usfx.jgroups;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ObjectMessage;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.util.Util;

/**
 * Nodo par de RemateUSFX. El primer miembro de la vista coordina temporalmente
 * las decisiones, pero todos ejecutan esta misma clase y conservan el estado.
 */
public final class NodoRemate implements Receiver, AutoCloseable {

    private static final String NOMBRE_GRUPO = "RemateSIS258";
    private static final long ESPERA_DECISION_MS = 5_000;

    private final Object candadoEstado = new Object();
    private final String participante;
    private final ExecutorService secuenciador = Executors.newSingleThreadExecutor(r -> {
        Thread hilo = new Thread(r, "remate-secuenciador");
        hilo.setDaemon(true);
        return hilo;
    });
    private final ScheduledExecutorService reloj = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread hilo = new Thread(r, "remate-reloj");
        hilo.setDaemon(true);
        return hilo;
    });
    private final Map<String, ScheduledFuture<?>> cierres = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Void>> confirmaciones = new ConcurrentHashMap<>();

    private volatile View vista;
    private EstadoRemate estado = new EstadoRemate();
    private JChannel canal;

    public NodoRemate(String participante) {
        this.participante = participante;
    }

    public void conectar() throws Exception {
        String configuracion = System.getProperty("config", "udp.xml");
        canal = new JChannel(configuracion);
        canal.name(participante);
        canal.setReceiver(this);
        canal.connect(NOMBRE_GRUPO);
        canal.getState(null, 10_000);
        System.out.println("** Conectado como " + participante
                + " usando " + configuracion);
    }

    @Override
    public void viewAccepted(View nuevaVista) {
        View anterior = vista;
        vista = nuevaVista;

        if (anterior != null) {
            Address[][] cambios = View.diff(anterior, nuevaVista);
            for (Address direccion : cambios[0]) {
                System.out.println("** ENTRO: " + direccion);
            }
            for (Address direccion : cambios[1]) {
                System.out.println("** SALIO: " + direccion);
            }
        }
        System.out.println("** Vista " + nuevaVista.getViewId().getId()
                + " | coordinador: " + nuevaVista.getCoord()
                + " | miembros: " + nuevaVista.getMembers());

        if (esCoordinador()) {
            secuenciador.execute(this::reprogramarTodosLosCierres);
        } else {
            cancelarCierresLocales();
        }
    }

    @Override
    public void receive(Message mensajeJGroups) {
        Object contenido = mensajeJGroups.getObject();
        if (!(contenido instanceof MensajeRemate mensaje)) {
            return;
        }

        if (mensaje.tipo().esSolicitud()) {
            recibirSolicitud(mensajeJGroups.getSrc(), mensaje);
        } else if (mensaje.tipo().modificaEstado()) {
            recibirDecision(mensaje);
        } else if (mensaje.tipo() == TipoMensaje.RECHAZO) {
            System.out.println("** RECHAZADO: " + mensaje.detalle());
        }
    }

    private void recibirSolicitud(Address origen, MensajeRemate solicitud) {
        // El callback de JGroups termina rápido; toda validación se serializa aparte.
        if (esCoordinador()) {
            secuenciador.execute(() -> procesarSolicitud(origen, solicitud));
            return;
        }

        Address coordinador = coordinadorActual();
        if (coordinador != null && !coordinador.equals(canal.getAddress())) {
            try {
                canal.send(new ObjectMessage(coordinador, solicitud));
            } catch (Exception e) {
                System.err.println("No se pudo reenviar la solicitud: " + e.getMessage());
            }
        }
    }

    private void procesarSolicitud(Address origen, MensajeRemate solicitud) {
        if (!esCoordinador()) {
            recibirSolicitud(origen, solicitud);
            return;
        }

        try {
            switch (solicitud.tipo()) {
                case SOLICITUD_CREAR -> procesarCreacion(origen, solicitud);
                case SOLICITUD_PUJA -> procesarPuja(origen, solicitud);
                case SOLICITUD_EXTENDER -> procesarExtension(origen, solicitud);
                default -> enviarRechazo(origen, solicitud.solicitante(),
                        "Solicitud desconocida");
            }
        } catch (Exception e) {
            enviarRechazo(origen, solicitud.solicitante(),
                    "No se pudo procesar la solicitud: " + e.getMessage());
        }
    }

    private void procesarCreacion(Address origen, MensajeRemate solicitud)
            throws Exception {
        MensajeRemate decision;
        synchronized (candadoEstado) {
            if (estado.buscar(solicitud.articulo()) != null) {
                enviarRechazo(origen, solicitud.solicitante(),
                        "Ya existe una subasta para '" + solicitud.articulo() + "'.");
                return;
            }
            if (solicitud.monto().compareTo(BigDecimal.ZERO) <= 0) {
                enviarRechazo(origen, solicitud.solicitante(),
                        "El precio base debe ser mayor que cero.");
                return;
            }
            if (solicitud.valorTiempo() <= 0) {
                enviarRechazo(origen, solicitud.solicitante(),
                        "La duración debe ser mayor que cero.");
                return;
            }
            long cierre = Math.addExact(System.currentTimeMillis(),
                    Math.multiplyExact(solicitud.valorTiempo(), 1_000));
            decision = MensajeRemate.subastaCreada(estado.getVersion() + 1,
                    solicitud.solicitante(), solicitud.articulo(),
                    solicitud.monto(), cierre);
        }
        difundirYEsperar(decision);
    }

    private void procesarPuja(Address origen, MensajeRemate solicitud)
            throws Exception {
        MensajeRemate decision;
        synchronized (candadoEstado) {
            Subasta subasta = estado.buscar(solicitud.articulo());
            if (subasta == null) {
                enviarRechazo(origen, solicitud.solicitante(),
                        "No existe la subasta '" + solicitud.articulo() + "'.");
                return;
            }
            if (!subasta.isAbierta() || System.currentTimeMillis() >= subasta.getCierreEpochMillis()) {
                enviarRechazo(origen, solicitud.solicitante(),
                        "La subasta '" + subasta.getArticulo() + "' ya cerró.");
                programarCierre(subasta.getArticulo(), subasta.getCierreEpochMillis());
                return;
            }
            if (solicitud.monto().compareTo(subasta.getMejorMonto()) <= 0) {
                enviarRechazo(origen, solicitud.solicitante(),
                        "La puja debe superar " + dinero(subasta.getMejorMonto()) + ".");
                return;
            }
            Puja puja = new Puja(UUID.randomUUID(), solicitud.solicitante(),
                    solicitud.monto(), System.currentTimeMillis());
            decision = MensajeRemate.pujaAceptada(
                    estado.getVersion() + 1, subasta.getArticulo(), puja);
        }
        difundirYEsperar(decision);
    }

    private void procesarExtension(Address origen, MensajeRemate solicitud)
            throws Exception {
        MensajeRemate decision;
        synchronized (candadoEstado) {
            Subasta subasta = estado.buscar(solicitud.articulo());
            if (subasta == null) {
                enviarRechazo(origen, solicitud.solicitante(),
                        "No existe la subasta '" + solicitud.articulo() + "'.");
                return;
            }
            if (!subasta.isAbierta() || System.currentTimeMillis() >= subasta.getCierreEpochMillis()) {
                enviarRechazo(origen, solicitud.solicitante(),
                        "No se puede extender una subasta cerrada.");
                return;
            }
            if (!subasta.getCreador().equals(solicitud.solicitante())) {
                enviarRechazo(origen, solicitud.solicitante(),
                        "Solo " + subasta.getCreador() + " puede extender esta subasta.");
                return;
            }
            if (solicitud.valorTiempo() <= 0) {
                enviarRechazo(origen, solicitud.solicitante(),
                        "La extensión debe ser mayor que cero.");
                return;
            }
            long nuevoCierre = Math.addExact(subasta.getCierreEpochMillis(),
                    Math.multiplyExact(solicitud.valorTiempo(), 1_000));
            decision = MensajeRemate.subastaExtendida(estado.getVersion() + 1,
                    solicitud.solicitante(), subasta.getArticulo(), nuevoCierre);
        }
        difundirYEsperar(decision);
    }

    private void recibirDecision(MensajeRemate decision) {
        boolean aplicada;
        synchronized (candadoEstado) {
            aplicada = estado.aplicar(decision);
        }

        CompletableFuture<Void> confirmacion = confirmaciones.remove(decision.id());
        if (confirmacion != null) {
            confirmacion.complete(null);
        }
        if (!aplicada) {
            return;
        }

        switch (decision.tipo()) {
            case SUBASTA_CREADA -> {
                System.out.println("** NUEVA SUBASTA: " + decision.articulo()
                        + " | base " + dinero(decision.monto())
                        + " | creador " + decision.solicitante());
                if (esCoordinador()) {
                    programarCierre(decision.articulo(), decision.valorTiempo());
                }
            }
            case PUJA_ACEPTADA -> System.out.println("** PUJA ACEPTADA: "
                    + decision.articulo() + " | " + decision.solicitante()
                    + " ofrece " + dinero(decision.monto()));
            case SUBASTA_EXTENDIDA -> {
                System.out.println("** SUBASTA EXTENDIDA: " + decision.articulo()
                        + " por " + decision.solicitante());
                if (esCoordinador()) {
                    programarCierre(decision.articulo(), decision.valorTiempo());
                }
            }
            case SUBASTA_CERRADA -> {
                ScheduledFuture<?> tarea = cierres.remove(EstadoRemate.clave(decision.articulo()));
                if (tarea != null) {
                    tarea.cancel(false);
                }
                if (decision.puja() == null) {
                    System.out.println("** CIERRE: " + decision.articulo()
                            + " terminó sin pujas.");
                } else {
                    System.out.println("** CIERRE: " + decision.articulo()
                            + " | ganador " + decision.puja().participante()
                            + " | monto " + dinero(decision.puja().monto()));
                }
            }
            default -> {
                // Cubierto por modificaEstado().
            }
        }
    }

    private void difundirYEsperar(MensajeRemate decision)
            throws Exception {
        CompletableFuture<Void> confirmacion = new CompletableFuture<>();
        confirmaciones.put(decision.id(), confirmacion);
        try {
            canal.send(new ObjectMessage(null, decision));
            confirmacion.get(ESPERA_DECISION_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("No se confirmó la decisión distribuida", e);
        } finally {
            confirmaciones.remove(decision.id());
        }
    }

    private void enviarRechazo(Address destino, String solicitante, String motivo) {
        try {
            canal.send(new ObjectMessage(destino,
                    MensajeRemate.rechazo(solicitante, motivo)));
        } catch (Exception e) {
            System.err.println("No se pudo enviar el rechazo: " + e.getMessage());
        }
    }

    private void reprogramarTodosLosCierres() {
        cancelarCierresLocales();
        List<Subasta> abiertas = new ArrayList<>();
        synchronized (candadoEstado) {
            for (Subasta subasta : estado.getSubastas()) {
                if (subasta.isAbierta()) {
                    abiertas.add(subasta);
                }
            }
        }
        for (Subasta subasta : abiertas) {
            programarCierre(subasta.getArticulo(), subasta.getCierreEpochMillis());
        }
    }

    private void programarCierre(String articulo, long instanteCierre) {
        if (!esCoordinador()) {
            return;
        }
        String clave = EstadoRemate.clave(articulo);
        ScheduledFuture<?> anterior = cierres.remove(clave);
        if (anterior != null) {
            anterior.cancel(false);
        }
        long demora = Math.max(0, instanteCierre - System.currentTimeMillis());
        ScheduledFuture<?> tarea = reloj.schedule(
                () -> secuenciador.execute(() -> cerrarSiCorresponde(articulo)),
                demora, TimeUnit.MILLISECONDS);
        cierres.put(clave, tarea);
    }

    private void cerrarSiCorresponde(String articulo) {
        if (!esCoordinador()) {
            return;
        }
        MensajeRemate decision;
        synchronized (candadoEstado) {
            Subasta subasta = estado.buscar(articulo);
            if (subasta == null || !subasta.isAbierta()) {
                return;
            }
            long restante = subasta.getCierreEpochMillis() - System.currentTimeMillis();
            if (restante > 0) {
                programarCierre(subasta.getArticulo(), subasta.getCierreEpochMillis());
                return;
            }
            decision = MensajeRemate.subastaCerrada(estado.getVersion() + 1,
                    subasta.getArticulo(), subasta.getMejorPuja());
        }
        try {
            difundirYEsperar(decision);
        } catch (Exception e) {
            System.err.println("No se pudo cerrar '" + articulo + "': " + e.getMessage());
        }
    }

    public void solicitarCrear(String articulo, BigDecimal base, long segundos)
            throws Exception {
        enviarAlCoordinador(MensajeRemate.solicitarCreacion(
                participante, articulo, base, segundos));
    }

    public void solicitarPuja(String articulo, BigDecimal monto) throws Exception {
        enviarAlCoordinador(MensajeRemate.solicitarPuja(
                participante, articulo, monto));
    }

    public void solicitarExtension(String articulo, long segundos) throws Exception {
        enviarAlCoordinador(MensajeRemate.solicitarExtension(
                participante, articulo, segundos));
    }

    private void enviarAlCoordinador(MensajeRemate solicitud) throws Exception {
        Address coordinador = coordinadorActual();
        if (coordinador == null) {
            throw new IllegalStateException("Todavía no existe una vista del grupo");
        }
        canal.send(new ObjectMessage(coordinador, solicitud));
    }

    public void imprimirSubastas() {
        long ahora = System.currentTimeMillis();
        synchronized (candadoEstado) {
            int abiertas = 0;
            for (Subasta subasta : estado.getSubastas()) {
                if (!subasta.isAbierta()) {
                    continue;
                }
                abiertas++;
                Puja mejor = subasta.getMejorPuja();
                long segundos = Math.max(0,
                        (subasta.getCierreEpochMillis() - ahora + 999) / 1_000);
                System.out.println(subasta.getArticulo()
                        + " | mejor " + dinero(subasta.getMejorMonto())
                        + " | por " + (mejor == null ? "sin pujas" : mejor.participante())
                        + " | restantes " + segundos + " s");
            }
            if (abiertas == 0) {
                System.out.println("No hay subastas abiertas.");
            }
        }
    }

    public void imprimirEstado(String articulo) {
        synchronized (candadoEstado) {
            Subasta subasta = estado.buscar(articulo);
            if (subasta == null) {
                System.out.println("No existe la subasta '" + articulo + "'.");
                return;
            }
            System.out.println("Historial de " + subasta.getArticulo() + ":");
            if (subasta.getPujas().isEmpty()) {
                System.out.println("  Sin pujas aceptadas.");
            }
            int numero = 1;
            for (Puja puja : subasta.getPujas()) {
                System.out.println("  " + numero++ + ". " + puja.participante()
                        + " -> " + dinero(puja.monto()));
            }
        }
    }

    public void imprimirParticipantes() {
        View actual = vista;
        if (actual == null) {
            System.out.println("Sin vista disponible.");
            return;
        }
        System.out.println("Participantes: " + actual.getMembers()
                + " | coordinador: " + actual.getCoord());
    }

    public void imprimirGanadas() {
        BigDecimal total = BigDecimal.ZERO;
        int cantidad = 0;
        synchronized (candadoEstado) {
            for (Subasta subasta : estado.getSubastas()) {
                Puja ganadora = subasta.getMejorPuja();
                if (!subasta.isAbierta() && ganadora != null
                        && participante.equals(ganadora.participante())) {
                    cantidad++;
                    total = total.add(ganadora.monto());
                    System.out.println(subasta.getArticulo() + " -> "
                            + dinero(ganadora.monto()));
                }
            }
        }
        System.out.println("Ganadas: " + cantidad + " | total a pagar: " + dinero(total));
    }

    private Address coordinadorActual() {
        View actual = vista;
        return actual == null ? null : actual.getCoord();
    }

    private boolean esCoordinador() {
        return canal != null && canal.isConnected()
                && canal.getAddress().equals(coordinadorActual());
    }

    private void cancelarCierresLocales() {
        for (ScheduledFuture<?> tarea : cierres.values()) {
            tarea.cancel(false);
        }
        cierres.clear();
    }

    static String dinero(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    public void getState(OutputStream salida) throws Exception {
        synchronized (candadoEstado) {
            Util.objectToStream(estado, new DataOutputStream(salida));
            System.out.println("** Estado enviado | versión " + estado.getVersion()
                    + " | subastas " + estado.getSubastas().size());
        }
    }

    @Override
    public void setState(InputStream entrada) throws Exception {
        EstadoRemate recibido = Util.objectFromStream(new DataInputStream(entrada));
        synchronized (candadoEstado) {
            estado = recibido;
            System.out.println("** Estado recibido | versión " + estado.getVersion()
                    + " | subastas " + estado.getSubastas().size());
        }
        if (esCoordinador()) {
            secuenciador.execute(this::reprogramarTodosLosCierres);
        }
    }

    @Override
    public void close() {
        cancelarCierresLocales();
        if (canal != null) {
            canal.close();
        }
        secuenciador.shutdownNow();
        reloj.shutdownNow();
    }
}
