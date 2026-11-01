package fr.zbar.showtime.backend.app.watchlist.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistMovieJpaRepository extends JpaRepository<WatchlistMovieJpaEntity, UUID> {

  Optional<WatchlistMovieJpaEntity> findByUserIdAndTmdbId(UUID userId, long tmdbId);

  List<WatchlistMovieJpaEntity> findByUserIdAndTmdbIdIn(UUID userId, Collection<Long> tmdbIds);
}
