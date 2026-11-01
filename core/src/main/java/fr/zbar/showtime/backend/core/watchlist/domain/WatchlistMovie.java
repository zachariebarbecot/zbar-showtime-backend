package fr.zbar.showtime.backend.core.watchlist.domain;

import fr.zbar.showtime.backend.core.shared.AggregateRoot;
import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class WatchlistMovie extends AggregateRoot<UUID> {

  UUID userId;
  long tmdbId;
  MovieStatus status;
  Instant addedAt;
  Instant watchedAt;

  public WatchlistMovie(
      UUID id, UUID userId, long tmdbId, MovieStatus status, Instant addedAt, Instant watchedAt) {
    super(id);
    this.userId = userId;
    this.tmdbId = tmdbId;
    this.status = status;
    this.addedAt = addedAt;
    this.watchedAt = watchedAt;
  }
}
