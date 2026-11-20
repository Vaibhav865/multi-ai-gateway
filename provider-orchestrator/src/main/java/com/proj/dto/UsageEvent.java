package com.proj.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
//@NoArgsConstructor
@RequiredArgsConstructor 
public class UsageEvent {
		
	private String userEmail;
    private String servedBy;
    private long latencyMs;
    private Instant timestamp;
	
    public UsageEvent(String email, String servedBy2, long latencyMs2) {
    	this.userEmail = userEmail;
    	this.servedBy = servedBy;
    	this.latencyMs = latencyMs;
    	this.timestamp = Instant.now();
    }
    
}
