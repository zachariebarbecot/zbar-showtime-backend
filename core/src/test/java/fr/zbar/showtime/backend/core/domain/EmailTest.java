package fr.zbar.showtime.backend.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

  @Test
  void should_keep_value_when_format_is_valid() {
    assertThat(new Email("a@b.fr").value()).isEqualTo("a@b.fr");
  }

  @Test
  void should_trim_and_lowercase_value_when_created() {
    assertThat(new Email(" Jean@Mail.FR ").value()).isEqualTo("jean@mail.fr");
  }

  @Test
  void should_be_equal_when_values_differ_only_by_case_and_surrounding_spaces() {
    assertThat(new Email("  A@B.fr ")).isEqualTo(new Email("a@b.FR"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "plainaddress", "a@", "@b.fr", "a@b", "a b@c.fr", "a@@b.fr"})
  void should_refuse_email_when_format_is_invalid(String value) {
    assertThatThrownBy(() -> new Email(value))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("email");
              assertThat(exception.messages()).containsExactly("Email must be a valid address");
            });
  }

  @Test
  void should_hide_value_when_converted_to_string() {
    assertThat(new Email("jean@mail.fr").toString()).doesNotContain("jean");
  }
}
