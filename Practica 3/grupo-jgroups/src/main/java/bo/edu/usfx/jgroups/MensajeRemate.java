package bo.edu.usfx.jgroups;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** Mensaje serializable del protocolo; las factorías evitan combinaciones inválidas. */
public record MensajeRemate(
        UUID id,
        TipoMensaje tipo,
        long version,
        String solicitante,
        String articulo,
        BigDecimal monto,
        long valorTiempo,
        Puja puja,
        String detalle) implements Serializable {

    public static MensajeRemate solicitarCreacion(
            String solicitante, String articulo, BigDecimal base, long segundos) {
        return nuevo(TipoMensaje.SOLICITUD_CREAR, 0, solicitante,
                articulo, base, segundos, null, null);
    }

    public static MensajeRemate subastaCreada(long version, String creador,
            String articulo, BigDecimal base, long instanteCierre) {
        return nuevo(TipoMensaje.SUBASTA_CREADA, version, creador,
                articulo, base, instanteCierre, null, null);
    }

    public static MensajeRemate solicitarPuja(
            String solicitante, String articulo, BigDecimal monto) {
        return nuevo(TipoMensaje.SOLICITUD_PUJA, 0, solicitante,
                articulo, monto, 0, null, null);
    }

    public static MensajeRemate pujaAceptada(
            long version, String articulo, Puja puja) {
        return nuevo(TipoMensaje.PUJA_ACEPTADA, version, puja.participante(),
                articulo, puja.monto(), puja.instanteEpochMillis(), puja, null);
    }

    public static MensajeRemate solicitarExtension(
            String solicitante, String articulo, long segundos) {
        return nuevo(TipoMensaje.SOLICITUD_EXTENDER, 0, solicitante,
                articulo, null, segundos, null, null);
    }

    public static MensajeRemate subastaExtendida(long version,
            String solicitante, String articulo, long nuevoCierre) {
        return nuevo(TipoMensaje.SUBASTA_EXTENDIDA, version, solicitante,
                articulo, null, nuevoCierre, null, null);
    }

    public static MensajeRemate subastaCerrada(
            long version, String articulo, Puja ganadora) {
        return nuevo(TipoMensaje.SUBASTA_CERRADA, version,
                ganadora == null ? null : ganadora.participante(), articulo,
                ganadora == null ? null : ganadora.monto(), 0, ganadora, null);
    }

    public static MensajeRemate rechazo(String solicitante, String detalle) {
        return nuevo(TipoMensaje.RECHAZO, 0, solicitante,
                null, null, 0, null, detalle);
    }

    private static MensajeRemate nuevo(TipoMensaje tipo, long version,
            String solicitante, String articulo, BigDecimal monto,
            long valorTiempo, Puja puja, String detalle) {
        return new MensajeRemate(UUID.randomUUID(), tipo, version, solicitante,
                articulo, monto, valorTiempo, puja, detalle);
    }
}
