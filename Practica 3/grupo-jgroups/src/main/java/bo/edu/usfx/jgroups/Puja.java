package bo.edu.usfx.jgroups;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record Puja(
        UUID id,
        String participante,
        BigDecimal monto,
        long instanteEpochMillis) implements Serializable {
}
