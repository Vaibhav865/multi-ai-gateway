package com.proj.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.proj.dto.OrchestratedResponse;

@Service
public class OrchestratorService {

    private final RestClient restClient;
    private final PromptCacheService cacheService;

    public OrchestratorService(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder loadBalancedRestClientBuilder,
            PromptCacheService cacheService) {
        this.restClient = loadBalancedRestClientBuilder.build();
        this.cacheService = cacheService;
    }

    public OrchestratedResponse route(String prompt, String authHeader) {
        // 1. Check the cache first
        String cached = cacheService.get(prompt);
        if (cached != null) {
            return new OrchestratedResponse(cached, "cache");
        }

        // 2. Cache miss: go to the AI providers
        OrchestratedResponse result = routeToProviders(prompt, authHeader);

        // 3. Store successful replies only (never cache the "all providers failed" message)
        if (!"none".equals(result.getServedBy())) {
            cacheService.put(prompt, result.getReply());
        }

        return result;
    }

    private OrchestratedResponse routeToProviders(String prompt, String authHeader) {
        // Primary: Groq (fastest)
        try {
            String reply = callAiProvider(prompt, "groq", authHeader);
            return new OrchestratedResponse(reply, "groq");
        } catch (Exception primaryFailure) {
            // Fallback: Gemini
            try {
                String reply = callAiProvider(prompt, "gemini", authHeader);
                return new OrchestratedResponse(reply, "gemini (fallback, groq failed)");
            } catch (Exception fallbackFailure) {
                return new OrchestratedResponse(
                    "All providers failed. Groq error: " + primaryFailure.getMessage()
                    + " | Gemini error: " + fallbackFailure.getMessage(),
                    "none"
                );
            }
        }
    }

    private String callAiProvider(String prompt, String provider, String authHeader) {
        Map<String, Object> requestBody = Map.of("prompt", prompt, "provider", provider);

        Map<String, Object> response = restClient.post()
                .uri("lb://AI-PROVIDER-SERVICE/api/chat")
                .header("Authorization", authHeader)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        return (String) response.get("reply");
    }
}