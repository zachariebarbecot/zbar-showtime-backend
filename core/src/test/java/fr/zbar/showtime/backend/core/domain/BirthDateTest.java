package fr.zbar.showtime.backend.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class BirthDateTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-03-01T23:59:59Z"), ZoneOffset.UTC);

  @Test
  void should_accept_yesterday() {
    LocalDate yesterday = LocalDate.of(2026, 2, 28);

    assertThat(new BirthDate(yesterday, CLOCK).value()).isEqualTo(yesterday);
  }

  @Test
  void should_refuse_today() {
    assertThatThrownBy(() -> new BirthDate(LocalDate.of(2026, 3, 1), CLOCK))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("birthDate");
              assertThat(exception.messages()).containsExactly("Birth date must be in the past");
            });
  }

  @Test
  void should_refuse_future_date() {
    assertThatThrownBy(() -> new BirthDate(LocalDate.of(2026, 3, 2), CLOCK))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception ->
                assertThat(exception.messages()).containsExactly("Birth date must be in the past"));
  }

  @Test
  void should_refuse_missing_date() {
    assertThatThrownBy(() -> new BirthDate(null, CLOCK))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("birthDate");
              assertThat(exception.messages()).containsExactly("Birth date cannot be null");
            });
  }

  @Test
  void should_hide_value_when_converted_to_string() {
    assertThat(new BirthDate(LocalDate.of(1990, 5, 17)).toString()).doesNotContain("1990");
  }
}
