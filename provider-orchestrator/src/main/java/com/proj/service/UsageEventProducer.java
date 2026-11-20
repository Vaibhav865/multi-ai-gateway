package com.proj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import com.proj.dto.UsageEvent;

@Service
public class UsageEventProducer {

	@Value("${usage.kafka.enabled:false}")
	private boolean enabled;

	private final KafkaTemplate<String, UsageEvent> kafkaTemplate;

	public UsageEventProducer(KafkaTemplate<String, UsageEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publish(UsageEvent event) {
		if (!enabled) return;
		try {
			kafkaTemplate.send("usage-events", event.getUserEmail(), event);
		} catch (Exception e) {
			System.err.println("Failed to publish usage event: " + e.getMessage());
		}
	}

}
