package com.proj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proj.dto.request.ChatRequest;
import com.proj.dto.response.ChatResponse;
import com.proj.service.GeminiService;
import com.proj.service.GroqService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
	
	@Autowired
	private  GeminiService geminiService;
	
	@Autowired
	private GroqService groqService;
	
	@PostMapping
	public ChatResponse chat (@RequestBody ChatRequest request) {
		String provider = request.getProvider() != null ? request.getProvider() : "gemini";
		
		String reply = switch(provider.toLowerCase()) {
		case "groq" -> groqService.getResponse(request.getPrompt());
		case "gemini" -> geminiService.getResponse(request.getPrompt());
		default -> "Unknown Provider: "+ provider;
		};
		
		return new ChatResponse(reply);
	}
	
}














