package bo.edu.usfx.jgroups;

/** Tipos explícitos del protocolo distribuido de RemateUSFX. */
public enum TipoMensaje {
    SOLICITUD_CREAR,
    SUBASTA_CREADA,
    SOLICITUD_PUJA,
    PUJA_ACEPTADA,
    SOLICITUD_EXTENDER,
    SUBASTA_EXTENDIDA,
    SUBASTA_CERRADA,
    RECHAZO;

    public boolean esSolicitud() {
        return this == SOLICITUD_CREAR
                || this == SOLICITUD_PUJA
                || this == SOLICITUD_EXTENDER;
    }

    public boolean modificaEstado() {
        return this == SUBASTA_CREADA
                || this == PUJA_ACEPTADA
                || this == SUBASTA_EXTENDIDA
                || this == SUBASTA_CERRADA;
    }
}
