package com.proj.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

@Service
public class PromptCacheService {

    private static final Duration TTL = Duration.ofHours(1);

    private final StringRedisTemplate redis;

    public PromptCacheService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String get(String prompt) {
        try {
            return redis.opsForValue().get(key(prompt));
        } catch (Exception e) {
            return null;
        }
    }

    public void put(String prompt, String reply) {
        try {
            redis.opsForValue().set(key(prompt), reply, TTL);
        } catch (Exception ignored) {
        }
    }

    private String key(String prompt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(prompt.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            return "ai:reply:" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}