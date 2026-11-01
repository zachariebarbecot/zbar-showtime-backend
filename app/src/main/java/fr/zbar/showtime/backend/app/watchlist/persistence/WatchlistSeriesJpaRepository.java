package fr.zbar.showtime.backend.app.watchlist.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistSeriesJpaRepository
    extends JpaRepository<WatchlistSeriesJpaEntity, UUID> {

  Optional<WatchlistSeriesJpaEntity> findByUserIdAndTmdbId(UUID userId, long tmdbId);
}
