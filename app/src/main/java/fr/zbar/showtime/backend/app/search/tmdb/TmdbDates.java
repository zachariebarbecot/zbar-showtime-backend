package fr.zbar.showtime.backend.app.search.tmdb;

import java.time.LocalDate;
import org.apache.commons.lang3.StringUtils;

final class TmdbDates {

  private TmdbDates() {}

  static LocalDate parse(String value) {
    return StringUtils.isEmpty(value) ? null : LocalDate.parse(value);
  }
}
