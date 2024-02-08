package com.task.account.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.ZoneId;

import static java.time.Instant.parse;

@TestConfiguration
@ActiveProfiles("test")
public class TestClockConfig {

  public static final String FIXED_TIME_STRING = "2024-01-01T13:10:00Z";
  public static final Clock CLOCK = Clock.fixed(
    parse(FIXED_TIME_STRING), ZoneId.of("UTC")
  );

  @Bean
  @Primary
  Clock clock() {
    return CLOCK;
  }
}
