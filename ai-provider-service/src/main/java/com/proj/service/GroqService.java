package com.proj.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();

    
//    private final RestClient restClient = RestClient.builder()
//            .requestInterceptor((request, body, execution) -> {
//                System.out.println("OUTGOING HEADERS: " + request.getHeaders());
//                return execution.execute(request, body);
//            })
//            .build();
    
    public String getResponse(String prompt) {
    	Map<String, Object> requestBody = Map.of(
    		    "model", "openai/gpt-oss-120b",
    		    "messages", List.of(
    		        Map.of("role", "user", "content", prompt)
    		    )
    		);

        System.out.println("GROQ KEY LOADED: [" + apiKey + "]");
        
        Map<String, Object> response = restClient.post()
                .uri(apiUrl)
                .headers(headers -> headers.setBearerAuth(apiKey))
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return "Error parsing Groq response: " + e.getMessage();
        }
    }
}