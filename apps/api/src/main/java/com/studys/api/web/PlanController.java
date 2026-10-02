package com.studys.api.web;

import com.studys.api.service.PlanOrchestrationService;
import com.studys.plans.PlanService;
import com.studys.schemas.plan.PlanRequest;
import com.studys.schemas.plan.PlanResponse;
import com.studys.schemas.plan.PlanStatusRequest;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/plans")
public class PlanController {

    private final PlanService plans;
    private final PlanOrchestrationService orchestrator;

    public PlanController(PlanService plans, PlanOrchestrationService orchestrator) {
        this.plans = plans;
        this.orchestrator = orchestrator;
    }

    @PostMapping
    public ResponseEntity<PlanResponse> criar(@RequestBody PlanRequest request) {
        PlanResponse criado = orchestrator.criarComArvore(request);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(localizacao).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<PlanResponse>> listar() {
        return ResponseEntity.ok(plans.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(plans.buscar(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PlanResponse> atualizarStatus(@PathVariable UUID id, @RequestBody PlanStatusRequest request) {
        String status = request == null ? null : request.getStatus();
        return ResponseEntity.ok(plans.atualizarStatus(id, status));
    }
}
