package com.sanad.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {

    /** Injected everywhere "now" is needed so tests can pin time. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
