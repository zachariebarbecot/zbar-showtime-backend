package fr.zbar.showtime.backend.core.watchlist.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SeriesStatusTest {

  @Test
  void should_read_known_values() {
    assertThat(SeriesStatus.from("TO_WATCH")).isEqualTo(SeriesStatus.TO_WATCH);
    assertThat(SeriesStatus.from("WATCHING")).isEqualTo(SeriesStatus.WATCHING);
    assertThat(SeriesStatus.from("WATCHED")).isEqualTo(SeriesStatus.WATCHED);
    assertThat(SeriesStatus.from("DROPPED")).isEqualTo(SeriesStatus.DROPPED);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"unknown", "watched", " WATCHED"})
  void should_refuse_value_outside_the_four_statuses(String value) {
    assertThatThrownBy(() -> SeriesStatus.from(value))
        .isInstanceOfSatisfying(
            InvalidFieldException.class,
            exception -> {
              assertThat(exception.field()).isEqualTo("status");
              assertThat(exception.messages())
                  .containsExactly("Status must be TO_WATCH, WATCHING, WATCHED or DROPPED");
            });
  }
}
