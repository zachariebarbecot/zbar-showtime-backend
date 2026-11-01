package fr.zbar.showtime.backend.core.search.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TmdbIdTest {

  @ParameterizedTest
  @ValueSource(longs = {1, 550, Long.MAX_VALUE})
  void should_accept_a_strictly_positive_id(long value) {
    assertThat(new TmdbId(value).value()).isEqualTo(value);
  }

  @ParameterizedTest
  @ValueSource(longs = {Long.MIN_VALUE, -1, 0})
  void should_refuse_a_non_positive_id(long value) {
    assertThatThrownBy(() -> new TmdbId(value))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("tmdbId");
              assertThat(exception.messages()).containsExactly("TMDB id must be greater than 0");
            });
  }
}
