package com.proj.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSecureController {

    @GetMapping("/api/secure/me")
    public String me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return "You are authenticated as: " + email;
    }
}