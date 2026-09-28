package com.proj.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.proj.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
		
		private final JwtAuthFilter jwtauthFilter;

	SecurityConfig(JwtAuthFilter jwtauthFilter) {
		this.jwtauthFilter = jwtauthFilter;
	}
		
		@Bean
		public SecurityFilterChain securityFilterChain(HttpSecurity http) {
			
			http.csrf((csrf) -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests( auth  -> auth.anyRequest().authenticated() )
				.addFilterBefore(jwtauthFilter, UsernamePasswordAuthenticationFilter.class);
			return http.build();
		}
			
}
