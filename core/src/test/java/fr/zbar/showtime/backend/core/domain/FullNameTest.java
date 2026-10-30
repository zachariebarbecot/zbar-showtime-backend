package fr.zbar.showtime.backend.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FullNameTest {

  @Test
  void should_keep_names_when_both_are_present() {
    FullName name = new FullName("Dupont", "Jean");

    assertThat(name.lastName()).isEqualTo("Dupont");
    assertThat(name.firstName()).isEqualTo("Jean");
  }

  @Test
  void should_store_trimmed_names_when_they_have_surrounding_spaces() {
    FullName name = new FullName("  Dupont  ", "  Jean ");

    assertThat(name.lastName()).isEqualTo("Dupont");
    assertThat(name.firstName()).isEqualTo("Jean");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  void should_refuse_name_when_last_name_is_missing(String lastName) {
    assertThatThrownBy(() -> new FullName(lastName, "Jean"))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("lastName");
              assertThat(exception.messages()).containsExactly("Last name cannot be blank");
            });
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  void should_refuse_name_when_first_name_is_missing(String firstName) {
    assertThatThrownBy(() -> new FullName("Dupont", firstName))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("firstName");
              assertThat(exception.messages()).containsExactly("First name cannot be blank");
            });
  }

  @Test
  void should_hide_names_when_converted_to_string() {
    assertThat(new FullName("Dupont", "Jean").toString()).doesNotContain("Dupont", "Jean");
  }
}
