package com.glady.deposit.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Configuration
public class TestConfiguration {

    @Bean
    public Clock clock() {
        Instant today = Instant.parse("2021-06-15T10:15:30.00+02:00");
        return Clock.fixed(today, ZoneId.of("Europe/Paris"));
    }
}
