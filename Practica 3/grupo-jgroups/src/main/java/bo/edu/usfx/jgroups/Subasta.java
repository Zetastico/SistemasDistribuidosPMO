package bo.edu.usfx.jgroups;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Subasta implements Serializable {

    private final String articulo;
    private final String creador;
    private final BigDecimal precioBase;
    private final List<Puja> pujas = new ArrayList<>();
    private long cierreEpochMillis;
    private boolean abierta = true;

    public Subasta(String articulo, String creador, BigDecimal precioBase,
            long cierreEpochMillis) {
        this.articulo = articulo;
        this.creador = creador;
        this.precioBase = precioBase;
        this.cierreEpochMillis = cierreEpochMillis;
    }

    public String getArticulo() {
        return articulo;
    }

    public String getCreador() {
        return creador;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public long getCierreEpochMillis() {
        return cierreEpochMillis;
    }

    public boolean isAbierta() {
        return abierta;
    }

    public List<Puja> getPujas() {
        return Collections.unmodifiableList(pujas);
    }

    public Puja getMejorPuja() {
        return pujas.isEmpty() ? null : pujas.get(pujas.size() - 1);
    }

    public BigDecimal getMejorMonto() {
        Puja mejor = getMejorPuja();
        return mejor == null ? precioBase : mejor.monto();
    }

    void registrarPuja(Puja puja) {
        pujas.add(puja);
    }

    void extenderHasta(long nuevoCierreEpochMillis) {
        cierreEpochMillis = nuevoCierreEpochMillis;
    }

    void cerrar() {
        abierta = false;
    }
}
