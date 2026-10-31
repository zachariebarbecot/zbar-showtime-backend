package fr.zbar.showtime.backend.core.domain;

import org.apache.commons.lang3.StringUtils;

public record SearchQuery(String value) {

  private static final int MIN_LENGTH = 2;
  private static final int MAX_LENGTH = 100;

  public SearchQuery {
    value = StringUtils.strip(value);
    if (value == null || value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
      throw new InvalidFieldException(
          "query", "Query must be between %d and %d characters".formatted(MIN_LENGTH, MAX_LENGTH));
    }
  }
}
