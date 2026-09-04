package bo.edu.usfx.jgroups;

public final class RemateUSFX {

    private RemateUSFX() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || args[0].isBlank()) {
            System.out.println("Uso: RemateUSFX <nombre-participante>");
            return;
        }

        try (NodoRemate nodo = new NodoRemate(args[0].trim())) {
            nodo.conectar();
            new ConsolaRemate(nodo).ejecutar();
        }
    }
}
