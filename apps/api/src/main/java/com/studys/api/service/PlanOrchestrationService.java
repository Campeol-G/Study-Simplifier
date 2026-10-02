package com.studys.api.service;

import com.studys.planengine.PlanEngineService;
import com.studys.plans.PlanService;
import com.studys.schemas.plan.PlanRequest;
import com.studys.schemas.plan.PlanResponse;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanOrchestrationService {

    private final PlanService plans;
    private final PlanEngineService engine;

    public PlanOrchestrationService(PlanService plans, PlanEngineService engine) {
        this.plans = plans;
        this.engine = engine;
    }

    @Transactional
    public PlanResponse criarComArvore(UUID userId, PlanRequest request) {
        PlanResponse criado = plans.criar(userId, request);
        engine.gerarArvoreEstatica(criado.getId());
        return plans.buscar(userId, criado.getId());
    }
}
