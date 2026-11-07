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
public class WatchedEpisode extends AggregateRoot<UUID> {

  UUID userId;
  long seriesTmdbId;
  int seasonNumber;
  int episodeNumber;
  Instant watchedAt;

  public WatchedEpisode(
      UUID id,
      UUID userId,
      long seriesTmdbId,
      int seasonNumber,
      int episodeNumber,
      Instant watchedAt) {
    super(id);
    this.userId = userId;
    this.seriesTmdbId = seriesTmdbId;
    this.seasonNumber = seasonNumber;
    this.episodeNumber = episodeNumber;
    this.watchedAt = watchedAt;
  }

  public static WatchedEpisode mark(
      UUID userId, long seriesTmdbId, int seasonNumber, int episodeNumber, Clock clock) {
    return new WatchedEpisode(
        UUID.randomUUID(), userId, seriesTmdbId, seasonNumber, episodeNumber, clock.instant());
  }
}
