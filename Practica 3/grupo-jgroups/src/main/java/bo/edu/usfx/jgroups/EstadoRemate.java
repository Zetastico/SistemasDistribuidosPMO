package bo.edu.usfx.jgroups;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Estado compartido que se replica y se transfiere a los miembros tardíos. */
public final class EstadoRemate implements Serializable {

    private final Map<String, Subasta> subastas = new LinkedHashMap<>();
    private long version;

    public long getVersion() {
        return version;
    }

    public Subasta buscar(String articulo) {
        return subastas.get(clave(articulo));
    }

    public Collection<Subasta> getSubastas() {
        return subastas.values();
    }

    /**
     * Aplica decisiones en la secuencia única producida por el coordinador.
     * Una decisión incluida en una transferencia de estado puede volver a llegar;
     * por eso las versiones antiguas se ignoran de forma idempotente.
     */
    public boolean aplicar(MensajeRemate mensaje) {
        if (!mensaje.tipo().modificaEstado()) {
            throw new IllegalArgumentException("El mensaje no modifica el estado");
        }
        if (mensaje.version() <= version) {
            return false;
        }
        if (mensaje.version() != version + 1) {
            throw new IllegalStateException("Secuencia incompleta: se esperaba "
                    + (version + 1) + " y llegó " + mensaje.version());
        }

        switch (mensaje.tipo()) {
            case SUBASTA_CREADA -> subastas.put(clave(mensaje.articulo()),
                    new Subasta(mensaje.articulo(), mensaje.solicitante(),
                            mensaje.monto(), mensaje.valorTiempo()));
            case PUJA_ACEPTADA -> exigirSubasta(mensaje.articulo())
                    .registrarPuja(mensaje.puja());
            case SUBASTA_EXTENDIDA -> exigirSubasta(mensaje.articulo())
                    .extenderHasta(mensaje.valorTiempo());
            case SUBASTA_CERRADA -> exigirSubasta(mensaje.articulo()).cerrar();
            default -> throw new IllegalStateException("Tipo de decisión inesperado");
        }
        version = mensaje.version();
        return true;
    }

    private Subasta exigirSubasta(String articulo) {
        Subasta subasta = buscar(articulo);
        if (subasta == null) {
            throw new IllegalStateException("No existe la subasta " + articulo);
        }
        return subasta;
    }

    static String clave(String articulo) {
        return articulo.trim().toLowerCase(Locale.ROOT);
    }
}
