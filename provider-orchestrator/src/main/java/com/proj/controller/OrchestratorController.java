package com.proj.controller;

import com.proj.dto.ChatRequest;
import com.proj.dto.OrchestratedResponse;
import com.proj.service.OrchestratorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ask")
public class OrchestratorController {

    private final OrchestratorService orchestratorService;

    public OrchestratorController(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping
    public OrchestratedResponse ask(@RequestBody ChatRequest request,
                                     @RequestHeader("Authorization") String authHeader) {
        return orchestratorService.route(request.getPrompt(), authHeader);
    }
}