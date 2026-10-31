package fr.zbar.showtime.backend.app.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class FixedClockConfiguration {

  public static final Instant NOW = Instant.parse("2025-06-15T10:00:00Z");

  @Bean
  @Primary
  Clock fixedClock() {
    return Clock.fixed(NOW, ZoneOffset.UTC);
  }
}
