package com.proj.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class OrchestratedResponse {
	
	private String reply;
    private String servedBy;
    
}
