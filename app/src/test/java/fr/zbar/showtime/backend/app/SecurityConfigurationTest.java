package fr.zbar.showtime.backend.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import org.junit.jupiter.api.Test;

class SecurityConfigurationTest {

  private final SecurityConfiguration securityConfiguration = new SecurityConfiguration();

  @Test
  void should_refuse_secret_shorter_than_32_bytes_without_revealing_it() {
    String shortSecret = "0123456789abcdef0123456789abcde";

    assertThatThrownBy(() -> securityConfiguration.jwtEncoder(shortSecret))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("app.jwt.secret must be at least 32 bytes in UTF-8");
  }

  @Test
  void should_accept_secret_of_32_bytes() {
    assertThat(securityConfiguration.jwtEncoder("0123456789abcdef0123456789abcdef")).isNotNull();
  }

  @Test
  void should_refuse_decoder_secret_shorter_than_32_bytes_without_revealing_it() {
    String shortSecret = "0123456789abcdef0123456789abcde";

    assertThatThrownBy(() -> securityConfiguration.jwtDecoder(shortSecret, Clock.systemUTC()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("app.jwt.secret must be at least 32 bytes in UTF-8");
  }

  @Test
  void should_accept_decoder_secret_of_32_bytes() {
    assertThat(
            securityConfiguration.jwtDecoder("0123456789abcdef0123456789abcdef", Clock.systemUTC()))
        .isNotNull();
  }
}
