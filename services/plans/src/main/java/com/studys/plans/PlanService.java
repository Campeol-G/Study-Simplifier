package com.studys.plans;

import com.studys.db.entity.PlanEntity;
import com.studys.db.entity.PlanNodeEntity;
import com.studys.db.entity.UserEntity;
import com.studys.db.repository.PlanNodeRepository;
import com.studys.db.repository.PlanRepository;
import com.studys.db.repository.UserRepository;
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
    private final UserRepository users;
    private final PlanNodeRepository nodes;

    public PlanService(PlanRepository plans, UserRepository users, PlanNodeRepository nodes) {
        this.plans = plans;
        this.users = users;
        this.nodes = nodes;
    }

    @Transactional
    public PlanResponse criar(UUID userId, PlanRequest request) {
        String assunto = request.getAssunto() == null || request.getAssunto().isBlank()
                ? null
                : request.getAssunto().trim();
        if (assunto == null) {
            throw new IllegalArgumentException("Assunto é obrigatório");
        }
        UserEntity user = users.findById(userId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário autenticado não encontrado"));
        PlanEntity plan = new PlanEntity(user, assunto, PlanStatus.ACTIVE.name());
        plan.setDescricao(request.getDescricao());
        return toResponse(plans.save(plan));
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listar(UUID userId) {
        return plans.findByUserIdOrderByCriadoEmDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse buscar(UUID userId, UUID planId) {
        return toResponse(buscarOuFalhar(userId, planId));
    }

    @Transactional
    public PlanResponse atualizarStatus(UUID userId, UUID planId, String rotulo) {
        PlanEntity plan = buscarOuFalhar(userId, planId);
        plan.setStatus(PlanStatusMapper.fromRotulo(rotulo).name());
        return toResponse(plans.save(plan));
    }

    private PlanEntity buscarOuFalhar(UUID userId, UUID planId) {
        return plans.findById(planId)
                .filter(p -> p.getUser().getId().equals(userId))
                .orElseThrow(() -> new PlanoNaoEncontradoException("Plano não encontrado"));
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
