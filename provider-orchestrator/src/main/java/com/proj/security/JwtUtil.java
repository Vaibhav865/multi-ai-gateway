package com.proj.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Configuration
public class JwtUtil {
		
		private final SecretKey key;
		
		public JwtUtil(@Value("${jwt.secret}") String secret) {
			this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		}
		
		public String extractEmail(String token) {
			return Jwts.parser()
					   .verifyWith(key)
					   .build()
					   .parseSignedClaims(token)
					   .getPayload()
					   .getSubject();
		}
		
		public boolean isTokenValid(String token) {
			
			try {
				Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
				return true;
			}catch(Exception e) {
				return false;
			}
			
		}
	
}
