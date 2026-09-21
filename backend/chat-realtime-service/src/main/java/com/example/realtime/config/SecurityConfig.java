package com.example.realtime.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration
 * For development: disable security
 * For production: enable JWT authentication
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Development mode - disable all security
     */
    @Bean
    @ConditionalOnProperty(name = "spring.security.enabled", havingValue = "false", matchIfMissing = true)
    public SecurityFilterChain developmentSecurityFilterChain(HttpSecurity http) throws Exception {
        log.warn("Security DISABLED - Development mode");
        
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        
        return http.build();
    }

    /**
     * Production mode - enable security with JWT
     * TODO: Implement JWT authentication in Task #10
     */
    @Bean
    @ConditionalOnProperty(name = "spring.security.enabled", havingValue = "true")
    public SecurityFilterChain productionSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("Security ENABLED - Production mode");
        
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**", "/actuator/**").permitAll()
                        .anyRequest().authenticated()
                );
        
        // TODO: Add JWT filter in Task #10
        
        return http.build();
    }
}
