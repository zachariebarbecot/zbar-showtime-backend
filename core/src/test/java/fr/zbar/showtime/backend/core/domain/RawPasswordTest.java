package fr.zbar.showtime.backend.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RawPasswordTest {

  private static final Email EMAIL = new Email("jean@mail.fr");

  @Test
  void should_have_no_violation_when_password_respects_the_policy() {
    assertThat(new RawPassword("Str0ng!Passw0rd").violations(EMAIL)).isEmpty();
  }

  @Test
  void should_accept_password_of_exactly_12_characters() {
    assertThat(new RawPassword("Abcdefgh1!ab").violations(EMAIL)).isEmpty();
  }

  @Test
  void should_accept_password_of_exactly_64_characters() {
    assertThat(new RawPassword("Ab1!" + "a".repeat(60)).violations(EMAIL)).isEmpty();
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      textBlock =
          """
          Abcdefgh1!a                                                        | Password must be at least 12 characters
          Ab1!aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa | Password must be at most 64 characters
          abcdefgh1!abcdef                                                   | Password must contain an uppercase letter
          ABCDEFGH1!ABCDEF                                                   | Password must contain a lowercase letter
          Abcdefgh!abcdef                                                    | Password must contain a digit
          Abcdefgh1abcdef                                                    | Password must contain a non-alphanumeric character
          Xjean@MAIL.fr1Ab!                                                  | Password must not contain the email
          """)
  void should_report_violated_rule_when_password_breaks_one_rule(String password, String message) {
    assertThat(new RawPassword(password.strip()).violations(EMAIL)).containsExactly(message);
  }

  @Test
  void should_report_72_bytes_limit_when_utf8_encoding_is_longer_than_72_bytes() {
    String password = "Ab1!" + "é".repeat(35);

    assertThat(password.length()).isLessThanOrEqualTo(64);
    assertThat(new RawPassword(password).violations(EMAIL))
        .containsExactly("Password must be at most 72 bytes in UTF-8");
  }

  @Test
  void should_report_every_violated_rule_when_password_breaks_several_rules() {
    assertThat(new RawPassword("jean@mail.fr").violations(EMAIL))
        .containsExactlyInAnyOrder(
            "Password must contain an uppercase letter",
            "Password must contain a digit",
            "Password must not contain the email");
  }

  @Test
  void should_hide_value_when_converted_to_string() {
    assertThat(new RawPassword("Str0ng!Passw0rd").toString()).doesNotContain("Str0ng");
  }

  @Test
  void should_refuse_password_when_missing() {
    assertThatThrownBy(() -> new RawPassword(null))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("password");
              assertThat(exception.messages()).containsExactly("Password must be provided");
            });
  }
}
