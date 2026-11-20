package com.proj.controller;

import com.proj.dto.request.AuthRequest;
import com.proj.dto.response.AuthResponse;
import com.proj.model.User;
import com.proj.repository.UserRepository;
import com.proj.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/ping")
    public String ping() {
        return "auth-service is alive";
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        String email = clean(request.getEmail());
        String password = request.getPassword();

        String problem = validate(email, password);
        if (problem != null) {
            return ResponseEntity.badRequest().body(problem);
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        return ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(email)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String email = clean(request.getEmail());
        String password = request.getPassword();

        if (email.isEmpty() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        return ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(email)));
    }

    private static String clean(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String validate(String email, String password) {
        if (email.isEmpty()) return "Email is required";
        if (!EMAIL.matcher(email).matches()) return "Enter a valid email address";
        if (password == null || password.isBlank()) return "Password is required";
        if (password.length() < MIN_PASSWORD_LENGTH) return "Password must be at least 6 characters";
        return null;
    }
}