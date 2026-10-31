package fr.zbar.showtime.backend.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class GreetingTest {

  @Test
  void should_be_equal_when_messages_are_equal() {
    assertThat(new Greeting("Hello World"))
        .isEqualTo(new Greeting("Hello World"))
        .hasSameHashCodeAs(new Greeting("Hello World"));
  }

  @Test
  void should_not_be_equal_when_messages_differ() {
    assertThat(new Greeting("Hello World")).isNotEqualTo(new Greeting("Bonjour"));
  }

  @Test
  void should_expose_its_message() {
    assertThat(new Greeting("Hello World").message()).isEqualTo("Hello World");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   "})
  void should_refuse_greeting_when_message_is_blank(String message) {
    assertThatThrownBy(() -> new Greeting(message))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("message");
              assertThat(exception.messages()).containsExactly("Message cannot be blank");
            });
  }
}
