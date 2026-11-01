package fr.zbar.showtime.backend.app.watchlist.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "watchlist_series")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
public class WatchlistSeriesJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "tmdb_id", nullable = false)
  private long tmdbId;

  @Column(name = "status", nullable = false, length = 16)
  private String status;

  @Column(name = "added_at", nullable = false)
  private Instant addedAt;

  @Column(name = "watched_at")
  private Instant watchedAt;
}
