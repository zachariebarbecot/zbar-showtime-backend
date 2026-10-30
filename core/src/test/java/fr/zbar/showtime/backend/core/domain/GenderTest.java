package fr.zbar.showtime.backend.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class GenderTest {

  @Test
  void should_read_known_values() {
    assertThat(Gender.from("MALE")).isEqualTo(Gender.MALE);
    assertThat(Gender.from("FEMALE")).isEqualTo(Gender.FEMALE);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"male", "AUTRE", "M"})
  void should_refuse_value_outside_male_and_female(String value) {
    assertThatThrownBy(() -> Gender.from(value))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("gender");
              assertThat(exception.messages()).containsExactly("Gender must be MALE or FEMALE");
            });
  }
}
