package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.InvalidFieldException;
import java.util.function.Supplier;

final class NamedField {

  private NamedField() {}

  static <T> T named(String field, Supplier<T> factory) {
    try {
      return factory.get();
    } catch (InvalidFieldException exception) {
      throw exception.onField(field);
    }
  }
}
