package com.proj.dto.request;

import lombok.Data;

@Data
public class ChatRequest {
	private String prompt;
    private String provider; // "gemini" or "groq"

}
