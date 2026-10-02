package com.studys.planengine;

import com.studys.db.entity.PlanEntity;
import com.studys.db.entity.PlanNodeEntity;
import com.studys.db.repository.PlanNodeRepository;
import com.studys.db.repository.PlanRepository;
import com.studys.schemas.plan.PlanNodeResponse;
import com.studys.schemas.plan.PlanNodeStatus;
import com.studys.schemas.plan.PlanNodeType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanEngineService {

    private static final int TOTAL_TOPICOS = 3;
    private static final int NIVEL_DOMINIO = 0;

    private final PlanRepository plans;
    private final PlanNodeRepository nodes;

    public PlanEngineService(PlanRepository plans, PlanNodeRepository nodes) {
        this.plans = plans;
        this.nodes = nodes;
    }

    @Transactional
    public void gerarArvoreEstatica(UUID planId) {
        PlanEntity plan = plans.findById(planId)
                .orElseThrow(() -> new NodePlanoNaoEncontradoException("Plano não encontrado para gerar a árvore"));
        String prefixoSlug = planId + "-";

        PlanNodeEntity raiz = new PlanNodeEntity(plan, null, plan.getAssunto(), NIVEL_DOMINIO, PlanNodeStatus.TODO.name());
        raiz.setTipo(PlanNodeType.MATERIA.name());
        raiz.setOrdem(0);
        raiz.setSlug(prefixoSlug + "materia");

        List<PlanNodeEntity> arvore = new ArrayList<>();
        arvore.add(raiz);
        for (int ordem = 1; ordem <= TOTAL_TOPICOS; ordem++) {
            PlanNodeEntity topico = new PlanNodeEntity(plan, raiz, "Tópico " + ordem, NIVEL_DOMINIO, PlanNodeStatus.TODO.name());
            topico.setTipo(PlanNodeType.TOPICO.name());
            topico.setOrdem(ordem);
            topico.setSlug(prefixoSlug + "topico-" + ordem);
            arvore.add(topico);
        }
        nodes.saveAll(arvore);
    }

    @Transactional
    public PlanNodeResponse atualizarStatusNode(UUID planId, UUID nodeId, PlanNodeStatus novoStatus) {
        PlanNodeEntity node = nodes.findById(nodeId)
                .filter(n -> n.getPlan().getId().equals(planId))
                .orElseThrow(() -> new NodePlanoNaoEncontradoException("Nó do plano não encontrado"));
        node.setStatus(novoStatus.name());
        return toResponse(nodes.save(node));
    }

    private static PlanNodeResponse toResponse(PlanNodeEntity node) {
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
