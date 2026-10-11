package com.sanad.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    /** Officer authentication (CCR-8) arrives in a later feature; citizen pages are public. */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.csrfTokenRequestHandler(eagerCsrfTokens()));
        return http.build();
    }

    /**
     * Resolve the CSRF token at the start of every request (BREACH-safe XOR encoding kept).
     * Deferred tokens would otherwise be created while Thymeleaf is streaming a large page,
     * after the response is committed, when a session can no longer be started.
     */
    static XorCsrfTokenRequestAttributeHandler eagerCsrfTokens() {
        XorCsrfTokenRequestAttributeHandler handler = new XorCsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName(null);
        return handler;
    }
}
