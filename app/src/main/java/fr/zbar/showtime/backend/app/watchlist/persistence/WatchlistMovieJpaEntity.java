package fr.zbar.showtime.backend.app.watchlist.persistence;

import fr.zbar.showtime.backend.app.user.persistence.UserJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "watchlist_movie",
    uniqueConstraints =
        @UniqueConstraint(
            name = "UC_watchlist_movie_user_tmdb",
            columnNames = {"user_id", "tmdb_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
public class WatchlistMovieJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private UserJpaEntity user;

  @Column(name = "tmdb_id", nullable = false)
  private long tmdbId;

  @Column(name = "status", nullable = false, length = 16)
  private String status;

  @Column(name = "added_at", nullable = false)
  private Instant addedAt;

  @Column(name = "watched_at")
  private Instant watchedAt;
}
