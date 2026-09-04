package bo.edu.usfx.jgroups;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Interfaz de consola: valida sintaxis y delega la lógica distribuida al nodo. */
public final class ConsolaRemate {

    private static final String NUMERO = "([0-9]+(?:[.,][0-9]+)?)";
    private static final Pattern CREAR = Pattern.compile(
            "^/crear\\s+(.+?)\\s+" + NUMERO + "\\s+([0-9]+)$");
    private static final Pattern PUJAR = Pattern.compile(
            "^/pujar\\s+(.+?)\\s+" + NUMERO + "$");
    private static final Pattern EXTENDER = Pattern.compile(
            "^/extender\\s+(.+?)\\s+([0-9]+)$");

    private final NodoRemate nodo;

    public ConsolaRemate(NodoRemate nodo) {
        this.nodo = nodo;
    }

    public void ejecutar() throws Exception {
        imprimirAyuda();
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
        String linea;
        while ((linea = teclado.readLine()) != null) {
            linea = linea.trim();
            if (linea.isEmpty()) {
                continue;
            }
            if (linea.equals("/salir")) {
                return;
            }
            try {
                procesar(linea);
            } catch (IllegalArgumentException e) {
                System.out.println("Entrada inválida: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("No se pudo ejecutar el comando: " + e.getMessage());
            }
        }
    }

    private void procesar(String linea) throws Exception {
        Matcher crear = CREAR.matcher(linea);
        if (crear.matches()) {
            nodo.solicitarCrear(crear.group(1), decimal(crear.group(2)),
                    Long.parseLong(crear.group(3)));
            return;
        }

        Matcher pujar = PUJAR.matcher(linea);
        if (pujar.matches()) {
            nodo.solicitarPuja(pujar.group(1), decimal(pujar.group(2)));
            return;
        }

        Matcher extender = EXTENDER.matcher(linea);
        if (extender.matches()) {
            nodo.solicitarExtension(extender.group(1),
                    Long.parseLong(extender.group(2)));
            return;
        }

        if (linea.equals("/subastas")) {
            nodo.imprimirSubastas();
        } else if (linea.startsWith("/estado ")) {
            nodo.imprimirEstado(linea.substring("/estado ".length()).trim());
        } else if (linea.equals("/quien")) {
            nodo.imprimirParticipantes();
        } else if (linea.equals("/ganadas")) {
            nodo.imprimirGanadas();
        } else if (linea.equals("/ayuda")) {
            imprimirAyuda();
        } else {
            throw new IllegalArgumentException("comando desconocido; use /ayuda");
        }
    }

    private BigDecimal decimal(String texto) {
        try {
            return new BigDecimal(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("el monto no es numérico");
        }
    }

    private void imprimirAyuda() {
        System.out.println("Comandos:");
        System.out.println("  /crear <artículo> <precio_base> <segundos>");
        System.out.println("  /subastas");
        System.out.println("  /pujar <artículo> <monto>");
        System.out.println("  /estado <artículo>");
        System.out.println("  /quien");
        System.out.println("  /ganadas");
        System.out.println("  /extender <artículo> <segundos>");
        System.out.println("  /salir");
    }
}
