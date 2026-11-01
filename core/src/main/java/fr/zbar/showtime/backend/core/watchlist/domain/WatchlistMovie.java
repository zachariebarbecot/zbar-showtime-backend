package fr.zbar.showtime.backend.core.watchlist.domain;

import fr.zbar.showtime.backend.core.shared.AggregateRoot;
import java.time.Clock;
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

  public WatchlistMovie changeStatus(MovieStatus newStatus, Clock clock) {
    if (status == newStatus) {
      return this;
    }
    Instant newWatchedAt = newStatus == MovieStatus.WATCHED ? clock.instant() : null;
    return new WatchlistMovie(id(), userId, tmdbId, newStatus, addedAt, newWatchedAt);
  }

  public static WatchlistMovie add(UUID userId, long tmdbId, Clock clock) {
    return new WatchlistMovie(
        UUID.randomUUID(), userId, tmdbId, MovieStatus.TO_WATCH, clock.instant(), null);
  }
}
