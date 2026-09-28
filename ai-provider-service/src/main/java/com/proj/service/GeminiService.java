package com.proj.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GeminiService {
		@Value("${gemini.api.key}")
		private String apiKey;
		
		@Value("${gemini.api.url}")
		private String apiUrl;

		private final RestClient restClient =RestClient.create();	
		
		public String getResponse(String prompt) {
			
			 Map<String, Object> requestBody = Map.of(
			            "contents", List.of(
			                Map.of("parts", List.of(
			                    Map.of("text", prompt)
			                ))
			            )
			        );
			 
			 Map<String, Object> response = restClient.post()
		                .uri(apiUrl + "?key=" + apiKey)
		                .header("Content-Type", "application/json")
		                .body(requestBody)
		                .retrieve()
		                .body(Map.class);

		        try {
		            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
		            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
		            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
		            return (String) parts.get(0).get("text");
		        } catch (Exception e) {
		            return "Error parsing Gemini response: " + e.getMessage();
		        }
			 
			
		}
}



















