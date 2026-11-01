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
public class WatchlistSeries extends AggregateRoot<UUID> {

  UUID userId;
  long tmdbId;
  SeriesStatus status;
  Instant addedAt;
  Instant watchedAt;

  public WatchlistSeries(
      UUID id, UUID userId, long tmdbId, SeriesStatus status, Instant addedAt, Instant watchedAt) {
    super(id);
    this.userId = userId;
    this.tmdbId = tmdbId;
    this.status = status;
    this.addedAt = addedAt;
    this.watchedAt = watchedAt;
  }

  public WatchlistSeries changeStatus(SeriesStatus newStatus, Clock clock) {
    return null;
  }

  public static WatchlistSeries add(UUID userId, long tmdbId, Clock clock) {
    return new WatchlistSeries(
        UUID.randomUUID(), userId, tmdbId, SeriesStatus.TO_WATCH, clock.instant(), null);
  }
}
