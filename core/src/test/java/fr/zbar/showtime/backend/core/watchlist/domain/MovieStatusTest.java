package fr.zbar.showtime.backend.core.watchlist.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MovieStatusTest {

  @Test
  void should_read_known_values() {
    assertThat(MovieStatus.from("TO_WATCH")).isEqualTo(MovieStatus.TO_WATCH);
    assertThat(MovieStatus.from("WATCHED")).isEqualTo(MovieStatus.WATCHED);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"WATCHING", "DROPPED", "watched", " WATCHED"})
  void should_refuse_value_outside_to_watch_and_watched(String value) {
    assertThatThrownBy(() -> MovieStatus.from(value))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("status");
              assertThat(exception.messages())
                  .containsExactly("Status must be TO_WATCH or WATCHED");
            });
  }
}
