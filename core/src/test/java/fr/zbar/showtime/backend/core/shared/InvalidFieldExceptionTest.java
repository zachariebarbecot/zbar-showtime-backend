package fr.zbar.showtime.backend.core.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class InvalidFieldExceptionTest {

  @Test
  void should_keep_the_messages_and_change_the_field_when_renamed() {
    InvalidFieldException exception = new InvalidFieldException("email", List.of("a", "b"));

    InvalidFieldException renamed = exception.onField("newEmail");

    assertThat(renamed.field()).isEqualTo("newEmail");
    assertThat(renamed.messages()).containsExactly("a", "b");
  }
}
