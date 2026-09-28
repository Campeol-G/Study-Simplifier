package com.studys.plans;

import com.studys.schemas.plan.PlanStatus;
import java.util.Locale;

public final class PlanStatusMapper {

    public static final String EM_ANDAMENTO = "em andamento";
    public static final String PAUSADO = "pausado";
    public static final String CONCLUIDO = "concluído";

    private PlanStatusMapper() {
    }

    public static PlanStatus fromRotulo(String rotulo) {
        String normalizado = rotulo == null ? "" : rotulo.trim().toLowerCase(Locale.ROOT);
        return switch (normalizado) {
            case EM_ANDAMENTO -> PlanStatus.ACTIVE;
            case PAUSADO -> PlanStatus.ARCHIVED;
            case CONCLUIDO, "concluido" -> PlanStatus.COMPLETED;
            default -> throw new StatusInvalidoException(
                    "Status inválido: use em andamento, pausado ou concluído");
        };
    }
}
