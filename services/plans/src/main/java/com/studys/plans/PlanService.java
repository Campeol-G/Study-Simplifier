package com.studys.plans;

import com.studys.db.entity.PlanEntity;
import com.studys.db.entity.PlanNodeEntity;
import com.studys.db.repository.PlanNodeRepository;
import com.studys.db.repository.PlanRepository;
import com.studys.schemas.plan.PlanNodeResponse;
import com.studys.schemas.plan.PlanNodeStatus;
import com.studys.schemas.plan.PlanNodeType;
import com.studys.schemas.plan.PlanRequest;
import com.studys.schemas.plan.PlanResponse;
import com.studys.schemas.plan.PlanStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanService {

    private final PlanRepository plans;
    private final PlanNodeRepository nodes;

    public PlanService(PlanRepository plans, PlanNodeRepository nodes) {
        this.plans = plans;
        this.nodes = nodes;
    }

    @Transactional
    public PlanResponse criar(PlanRequest request) {
        String assunto = request.getAssunto() == null || request.getAssunto().isBlank()
                ? null
                : request.getAssunto().trim();
        if (assunto == null) {
            throw new IllegalArgumentException("Assunto é obrigatório");
        }
        PlanEntity plan = new PlanEntity(assunto, PlanStatus.ACTIVE.name());
        plan.setDescricao(request.getDescricao());
        return toResponse(plans.save(plan));
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listar() {
        return plans.findAllByOrderByCriadoEmDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse buscar(UUID planId) {
        return toResponse(plans.findById(planId)
                .orElseThrow(() -> new PlanoNaoEncontradoException("Plano não encontrado")));
    }

    @Transactional
    public PlanResponse atualizarStatus(UUID planId, String rotulo) {
        PlanEntity plan = plans.findById(planId)
                .orElseThrow(() -> new PlanoNaoEncontradoException("Plano não encontrado"));
        plan.setStatus(PlanStatusMapper.fromRotulo(rotulo).name());
        return toResponse(plans.save(plan));
    }

    private PlanResponse toResponse(PlanEntity plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getAssunto(),
                plan.getDescricao(),
                PlanStatus.valueOf(plan.getStatus()),
                plan.getCriadoEm(),
                nodes.findByPlanIdOrderByOrdemAsc(plan.getId()).stream()
                        .map(PlanService::toNodeResponse)
                        .toList());
    }

    private static PlanNodeResponse toNodeResponse(PlanNodeEntity node) {
        return new PlanNodeResponse(
                node.getId(),
                node.getTitulo(),
                node.getSlug(),
                PlanNodeType.valueOf(node.getTipo()),
                PlanNodeStatus.valueOf(node.getStatus()),
                node.getOrdem(),
                node.getNivelDominio(),
                node.getParent() == null ? null : node.getParent().getId());
    }
}
