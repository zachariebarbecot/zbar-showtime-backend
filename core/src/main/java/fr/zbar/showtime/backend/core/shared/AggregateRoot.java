package fr.zbar.showtime.backend.core.shared;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AggregateRoot<ID> {

  private final ID id;

  public ID id() {
    return id;
  }

  @Override
  public boolean equals(Object other) {
    return this == other
        || other != null
            && getClass() == other.getClass()
            && id.equals(((AggregateRoot<?>) other).id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
