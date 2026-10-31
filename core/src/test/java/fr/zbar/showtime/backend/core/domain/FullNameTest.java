package fr.zbar.showtime.backend.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
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

  @Test
  void should_accept_names_of_100_characters() {
    String name = "a".repeat(100);

    assertThat(new FullName(name, name)).isEqualTo(new FullName(name, name));
  }

  @Test
  void should_refuse_name_when_last_name_is_longer_than_100_characters() {
    assertThatThrownBy(() -> new FullName("a".repeat(101), "Jean"))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("lastName");
              assertThat(exception.messages())
                  .containsExactly("Last name must be at most 100 characters");
            });
  }

  @Test
  void should_refuse_name_when_first_name_is_longer_than_100_characters() {
    assertThatThrownBy(() -> new FullName("Dupont", "a".repeat(101)))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("firstName");
              assertThat(exception.messages())
                  .containsExactly("First name must be at most 100 characters");
            });
  }
}
