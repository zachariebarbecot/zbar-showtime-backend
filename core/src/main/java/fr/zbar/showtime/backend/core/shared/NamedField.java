package fr.zbar.showtime.backend.core.shared;

import java.util.function.Supplier;

public final class NamedField {

  private NamedField() {}

  public static <T> T named(String field, Supplier<T> factory) {
    try {
      return factory.get();
    } catch (InvalidFieldException exception) {
      throw exception.onField(field);
    }
  }
}
