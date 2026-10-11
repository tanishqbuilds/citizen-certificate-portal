package com.sanad.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

/**
 * Citizens are anonymous; officers sign in with a form (CCR-8).
 * Everything under /officer requires the OFFICER role, except the sign-in page itself.
 */
@Configuration
public class SecurityConfig {

    public static final String ROLE_OFFICER = "OFFICER";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/officer/login").permitAll()
                        .requestMatchers("/officer/**").hasRole(ROLE_OFFICER)
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/officer/login")
                        .loginProcessingUrl("/officer/login")
                        .defaultSuccessUrl("/officer", true)
                        .failureUrl("/officer/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/officer/logout")
                        .logoutSuccessUrl("/officer/login?signedOut")
                        .deleteCookies("JSESSIONID"))
                .sessionManagement(session -> session.sessionFixation(fixation -> fixation.changeSessionId()))
                .headers(headers -> headers
                        .referrerPolicy(referrer -> referrer.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)))
                .csrf(csrf -> csrf.csrfTokenRequestHandler(eagerCsrfTokens()));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** One reviewer account for the MVP; the password is BCrypt-hashed at start-up and never kept in plain text. */
    @Bean
    UserDetailsService officers(AppProperties props, PasswordEncoder encoder) {
        AppProperties.Officer officer = props.officer();
        if (officer.password() == null || officer.password().length() < 8) {
            throw new IllegalStateException("app.officer.password (APP_OFFICER_PASSWORD) must be at least 8 characters");
        }
        return new InMemoryUserDetailsManager(User.withUsername(officer.username())
                .password(encoder.encode(officer.password()))
                .roles(ROLE_OFFICER)
                .build());
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
