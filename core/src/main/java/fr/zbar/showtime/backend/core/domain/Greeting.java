package fr.zbar.showtime.backend.core.domain;

import org.apache.commons.lang3.StringUtils;

public record Greeting(String message) {

  public Greeting {
    if (StringUtils.isBlank(message)) {
      throw new InvalidFieldException("message", "Message cannot be blank");
    }
  }
}
