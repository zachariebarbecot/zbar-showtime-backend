package fr.zbar.showtime.backend.core.domain;

import org.apache.commons.lang3.StringUtils;

public record SearchQuery(String value) {

  public SearchQuery {
    value = StringUtils.strip(value);
  }
}
