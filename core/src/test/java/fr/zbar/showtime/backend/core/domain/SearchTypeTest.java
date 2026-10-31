package fr.zbar.showtime.backend.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SearchTypeTest {

  @Test
  void should_read_known_values() {
    assertThat(SearchType.from("MOVIE")).isEqualTo(SearchType.MOVIE);
    assertThat(SearchType.from("TV")).isEqualTo(SearchType.TV);
    assertThat(SearchType.from("ALL")).isEqualTo(SearchType.ALL);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "movie", "tv", "BOOK", "all"})
  void should_refuse_value_outside_movie_tv_and_all(String value) {
    assertThatThrownBy(() -> SearchType.from(value))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("type");
              assertThat(exception.messages()).containsExactly("Type must be MOVIE, TV or ALL");
            });
  }
}
