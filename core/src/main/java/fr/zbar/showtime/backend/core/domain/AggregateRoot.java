package fr.zbar.showtime.backend.core.domain;

import java.util.Objects;

public abstract class AggregateRoot<ID> {

  private final ID id;

  protected AggregateRoot(ID id) {
    this.id = id;
  }

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
