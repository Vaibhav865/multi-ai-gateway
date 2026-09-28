package com.proj.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.proj.dto.OrchestratedResponse;

//import jakarta.inject.Qualifier;

@Service
public class OrchestratorService {

	private RestClient restClient;

	public OrchestratorService(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder loadBalancedRestClientBuilder) {
	    this.restClient = loadBalancedRestClientBuilder.build();
	}

    public OrchestratedResponse route(String prompt, String authHeader) {
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