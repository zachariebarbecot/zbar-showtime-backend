package fr.zbar.showtime.backend.core.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordHashTest {

  @Test
  void should_hide_value_when_converted_to_string() {
    assertThat(new PasswordHash("$2a$10$secret").toString()).doesNotContain("secret");
  }
}
