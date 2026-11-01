package fr.zbar.showtime.backend.app.search.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TmdbDatesTest {

  @ParameterizedTest
  @NullAndEmptySource
  void should_return_null_when_date_is_absent_or_empty(String value) {
    assertThat(TmdbDates.parse(value)).isNull();
  }

  @Test
  void should_return_local_date_when_date_is_valid() {
    assertThat(TmdbDates.parse("2020-05-17")).isEqualTo(LocalDate.of(2020, 5, 17));
  }

  @ParameterizedTest
  @ValueSource(strings = {"2020-13-45", "abc", " "})
  void should_throw_exception_when_date_is_malformed(String value) {
    assertThatThrownBy(() -> TmdbDates.parse(value)).isInstanceOf(DateTimeParseException.class);
  }
}
