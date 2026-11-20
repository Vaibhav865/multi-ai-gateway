package com.proj.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proj.dto.ChatRequest;
import com.proj.dto.OrchestratedResponse;
import com.proj.dto.UsageEvent;
import com.proj.security.JwtUtil;
import com.proj.service.OrchestratorService;
import com.proj.service.UsageEventProducer;

@RestController
@RequestMapping("/api/ask")
public class OrchestratorController {

	private final OrchestratorService orchestratorService;
	private final UsageEventProducer usageEventProducer;
	private final JwtUtil jwtUtil;

	public OrchestratorController(OrchestratorService orchestratorService, UsageEventProducer usageEventProducer,
			JwtUtil jwtUtil) {
		this.orchestratorService = orchestratorService;
		this.usageEventProducer = usageEventProducer;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping
	public OrchestratedResponse ask(@RequestBody ChatRequest request,
			@RequestHeader("Authorization") String authHeader) {
		long start = System.currentTimeMillis();
		OrchestratedResponse response = orchestratorService.route(request.getPrompt(), authHeader);
		response.setLatencyMs(System.currentTimeMillis() - start);

		String email = jwtUtil.extractEmail(authHeader.replace("Bearer ", ""));
		usageEventProducer.publish(new UsageEvent(email, response.getServedBy(), response.getLatencyMs()));

		return response;
	}

}