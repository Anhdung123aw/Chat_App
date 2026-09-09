package com.example.chatcore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity // Enables @PreAuthorize, @Secured, @RolesAllowed
public class SecurityConfig {

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    /**
     * Phase 2 Roles:
     * - ROLE_AGENT: Access to agent APIs (conversation management, canned replies)
     * - ROLE_SUPERVISOR: Access to supervisor dashboard, reports, SLA monitoring
     * - ROLE_ADMIN: Full access to all APIs including config management
     * 
     * Use @PreAuthorize("hasRole('AGENT')") on controller methods
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (securityEnabled) {
            // Production mode: OAuth2 JWT required
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            // Phase 2: Public rating API (customers can rate without auth)
                            .requestMatchers("/api/v1/ratings/conversations/*/submit").permitAll()
                            .anyRequest().authenticated()
                    )
                    .oauth2ResourceServer(oauth2 -> oauth2
                            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                    );
        } else {
            // Development mode: Permit all
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.get("roles") == null) {
            return List.of();
        }
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
