package fr.zbar.showtime.backend.core.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import fr.zbar.showtime.backend.core.watchlist.application.InMemoryWatchlistMovieRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.MovieStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GetMovieDetailServiceTest {

  private static final MovieDetail MATRIX =
      new MovieDetail(
          603L,
          "Matrix",
          "The Matrix",
          List.of("Action"),
          LocalDate.of(1999, 3, 31),
          136,
          fr.zbar.showtime.backend.core.search.domain.MovieStatus.RELEASED,
          "Un hacker découvre la réalité.",
          "https://image.tmdb.org/t/p/w342/m.jpg",
          List.of(),
          List.of(),
          List.of());

  private static final UUID USER = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_USER = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private final List<WatchlistMovie> watchlist = new ArrayList<>();

  private final GetMovieDetailService service =
      new GetMovieDetailService(
          new InMemoryMovieDetailCatalog(MATRIX), new InMemoryWatchlistMovieRepository(watchlist));

  private static WatchlistMovie inWatchlist(UUID user, long tmdbId, MovieStatus status) {
    return new WatchlistMovie(UUID.randomUUID(), user, tmdbId, status, Instant.EPOCH, null);
  }

  @Test
  void should_return_the_movie_detail_when_the_id_is_known() {
    assertThat(service.execute(new GetMovieDetailCommand(USER, 603L)).movie()).isEqualTo(MATRIX);
  }

  @Test
  void should_expose_the_watchlist_status_of_the_caller_when_the_movie_is_in_the_watchlist() {
    watchlist.add(inWatchlist(USER, 603L, MovieStatus.WATCHED));

    assertThat(service.execute(new GetMovieDetailCommand(USER, 603L)).watchlistStatus())
        .isEqualTo("WATCHED");
  }

  @Test
  void should_expose_no_watchlist_status_when_the_movie_is_not_in_the_watchlist() {
    MovieDetailView view = service.execute(new GetMovieDetailCommand(USER, 603L));

    assertThat(view.watchlistStatus()).isNull();
    assertThat(view.movie()).isEqualTo(MATRIX);
  }

  @Test
  void should_expose_no_watchlist_status_when_only_another_user_has_the_movie() {
    watchlist.add(inWatchlist(OTHER_USER, 603L, MovieStatus.WATCHED));

    assertThat(service.execute(new GetMovieDetailCommand(USER, 603L)).watchlistStatus()).isNull();
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, -1L})
  void should_throw_invalid_field_without_calling_the_catalog_when_the_id_is_not_positive(
      long tmdbId) {
    GetMovieDetailService unreachableCatalog =
        new GetMovieDetailService(
            id -> {
              throw new AssertionError("catalog must not be called");
            },
            new InMemoryWatchlistMovieRepository(watchlist));

    assertThatThrownBy(() -> unreachableCatalog.execute(new GetMovieDetailCommand(USER, tmdbId)))
        .isInstanceOf(InvalidFieldException.class)
        .hasMessage("tmdbId: TMDB id must be greater than 0");
  }
}
