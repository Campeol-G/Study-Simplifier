package com.studys.api.web;

import com.studys.planengine.PlanEngineService;
import com.studys.schemas.plan.PlanNodeResponse;
import com.studys.schemas.plan.PlanNodeStatus;
import com.studys.schemas.plan.PlanNodeStatusRequest;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plans/{planId}/nodes")
public class PlanNodeController {

    private final PlanEngineService engine;

    public PlanNodeController(PlanEngineService engine) {
        this.engine = engine;
    }

    @PatchMapping("/{nodeId}")
    public ResponseEntity<PlanNodeResponse> atualizarStatus(@PathVariable UUID planId, @PathVariable UUID nodeId,
            @RequestBody PlanNodeStatusRequest request) {
        PlanNodeStatus status = lerStatus(request);
        return ResponseEntity.ok(engine.atualizarStatusNode(usuarioAtual(), planId, nodeId, status));
    }

    // Status de no nao tem rotulo (o cliente manda o nome do enum), por isso o parse e direto aqui, sem o
    // PlanStatusMapper que PlanController.atualizarStatus usa para os rotulos em portugues.
    private PlanNodeStatus lerStatus(PlanNodeStatusRequest request) {
        String status = request == null ? null : request.getStatus();
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status é obrigatório");
        }
        return PlanNodeStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
    }

    private UUID usuarioAtual() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
