package fr.zbar.showtime.backend.core.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.PageNumber;
import fr.zbar.showtime.backend.core.search.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.search.domain.SearchPage;
import fr.zbar.showtime.backend.core.search.domain.SearchQuery;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import fr.zbar.showtime.backend.core.watchlist.application.InMemoryWatchlistMovieRepository;
import fr.zbar.showtime.backend.core.watchlist.application.InMemoryWatchlistSeriesRepository;
import fr.zbar.showtime.backend.core.watchlist.domain.MovieStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.SeriesStatus;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistMovie;
import fr.zbar.showtime.backend.core.watchlist.domain.WatchlistSeries;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SearchTitlesServiceTest {

  private static final CatalogItem MATRIX =
      new CatalogItem(
          603L,
          MediaType.MOVIE,
          "Matrix",
          "https://image.tmdb.org/t/p/w342/matrix.jpg",
          LocalDate.of(1999, 3, 31));

  private static final CatalogItem DARK =
      new CatalogItem(
          70523L,
          MediaType.TV,
          "Dark",
          "https://image.tmdb.org/t/p/w342/dark.jpg",
          LocalDate.of(2017, 12, 1));

  private static final UUID USER = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_USER = UUID.fromString("22222222-2222-2222-2222-222222222222");

  private static final Clock CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

  private final List<WatchlistMovie> watchlist = new ArrayList<>();
  private final List<WatchlistSeries> seriesWatchlist = new ArrayList<>();

  private final SearchTitlesService service =
      new SearchTitlesService(
          new InMemorySearchCatalog(MATRIX, DARK),
          new InMemoryWatchlistMovieRepository(watchlist),
          new InMemoryWatchlistSeriesRepository(seriesWatchlist));

  private static WatchlistMovie inWatchlist(UUID user, long tmdbId, MovieStatus status) {
    return new WatchlistMovie(UUID.randomUUID(), user, tmdbId, status, Instant.EPOCH, null);
  }

  private static WatchlistSeries seriesInWatchlist(UUID user, long tmdbId, SeriesStatus status) {
    return new WatchlistSeries(UUID.randomUUID(), user, tmdbId, status, Instant.EPOCH, null);
  }

  @Test
  void should_search_the_catalog_with_the_trimmed_query_and_requested_page() {
    SearchResultPage page =
        service.execute(new SearchTitlesCommand(USER, "  Matrix  ", 3, "MOVIE"));

    assertThat(page.page()).isEqualTo(3);
    assertThat(page.results()).extracting(SearchResultItem::item).containsExactly(MATRIX);
  }

  @Test
  void should_search_the_series_when_the_type_is_tv() {
    SearchResultPage page = service.execute(new SearchTitlesCommand(USER, "Dark", 1, "TV"));

    assertThat(page.results()).extracting(SearchResultItem::item).containsExactly(DARK);
  }

  @Test
  void should_search_movies_and_series_together_when_the_type_is_all() {
    SearchTitlesService mixed =
        new SearchTitlesService(
            new InMemorySearchCatalog(
                MATRIX, new CatalogItem(1L, MediaType.TV, "Matrix", null, null)),
            new InMemoryWatchlistMovieRepository(watchlist),
            new InMemoryWatchlistSeriesRepository(seriesWatchlist));

    SearchResultPage page = mixed.execute(new SearchTitlesCommand(USER, "matrix", 1, "ALL"));

    assertThat(page.results())
        .extracting(result -> result.item().type())
        .containsExactly(MediaType.MOVIE, MediaType.TV);
  }

  @Test
  void should_reject_without_calling_the_catalog_when_the_query_or_the_page_is_invalid() {
    SearchTitlesService unreachableCatalog =
        new SearchTitlesService(
            new SearchCatalog() {
              @Override
              public SearchPage searchMovies(SearchQuery query, PageNumber page) {
                throw new AssertionError("TMDB must not be called");
              }

              @Override
              public SearchPage searchSeries(SearchQuery query, PageNumber page) {
                throw new AssertionError("TMDB must not be called");
              }

              @Override
              public SearchPage searchAll(SearchQuery query, PageNumber page) {
                throw new AssertionError("TMDB must not be called");
              }
            },
            new InMemoryWatchlistMovieRepository(watchlist),
            new InMemoryWatchlistSeriesRepository(seriesWatchlist));

    assertThatThrownBy(
            () -> unreachableCatalog.execute(new SearchTitlesCommand(USER, "a", 1, "MOVIE")))
        .isInstanceOf(InvalidFieldException.class);
    assertThatThrownBy(
            () -> unreachableCatalog.execute(new SearchTitlesCommand(USER, "matrix", 501, "MOVIE")))
        .isInstanceOf(InvalidFieldException.class);
    assertThatThrownBy(
            () -> unreachableCatalog.execute(new SearchTitlesCommand(USER, "matrix", 1, "BOOK")))
        .isInstanceOf(InvalidFieldException.class);
  }

  @Test
  void should_expose_the_watchlist_status_of_the_movie_when_it_is_in_the_watchlist() {
    watchlist.add(inWatchlist(USER, 603L, MovieStatus.WATCHED));

    SearchResultPage page = service.execute(new SearchTitlesCommand(USER, "Matrix", 1, "MOVIE"));

    assertThat(page.results())
        .extracting(SearchResultItem::watchlistStatus)
        .containsExactly("WATCHED");
  }

  @Test
  void should_expose_no_status_when_the_movie_is_not_in_the_watchlist_of_the_user() {
    watchlist.add(inWatchlist(OTHER_USER, 603L, MovieStatus.WATCHED));

    SearchResultPage page = service.execute(new SearchTitlesCommand(USER, "Matrix", 1, "MOVIE"));

    assertThat(page.results()).extracting(SearchResultItem::watchlistStatus).containsOnlyNulls();
  }

  @Test
  void should_expose_the_new_status_when_the_status_of_the_movie_changed() {
    WatchlistMovie entry = WatchlistMovie.add(USER, 603L, CLOCK);
    InMemoryWatchlistMovieRepository repository = new InMemoryWatchlistMovieRepository(watchlist);
    repository.save(entry);
    SearchTitlesCommand command = new SearchTitlesCommand(USER, "Matrix", 1, "MOVIE");

    SearchResultPage before = service.execute(command);
    repository.save(entry.changeStatus(MovieStatus.WATCHED, CLOCK));
    SearchResultPage after = service.execute(command);

    assertThat(before.results())
        .extracting(SearchResultItem::watchlistStatus)
        .containsExactly("TO_WATCH");
    assertThat(after.results())
        .extracting(SearchResultItem::watchlistStatus)
        .containsExactly("WATCHED");
  }

  @Test
  void should_keep_the_catalog_order_and_totals_with_each_movie_carrying_its_own_status() {
    CatalogItem first = new CatalogItem(1L, MediaType.MOVIE, "Matrix", null, null);
    CatalogItem second = new CatalogItem(2L, MediaType.MOVIE, "Matrix", null, null);
    CatalogItem third = new CatalogItem(3L, MediaType.MOVIE, "Matrix", null, null);
    watchlist.add(inWatchlist(USER, 1L, MovieStatus.TO_WATCH));
    watchlist.add(inWatchlist(USER, 3L, MovieStatus.WATCHED));
    SearchTitlesService mixed =
        new SearchTitlesService(
            new InMemorySearchCatalog(first, second, third),
            new InMemoryWatchlistMovieRepository(watchlist),
            new InMemoryWatchlistSeriesRepository(seriesWatchlist));

    SearchResultPage page = mixed.execute(new SearchTitlesCommand(USER, "Matrix", 2, "ALL"));

    assertThat(page.page()).isEqualTo(2);
    assertThat(page.totalResults()).isEqualTo(3);
    assertThat(page.results())
        .extracting(result -> result.item().id(), SearchResultItem::watchlistStatus)
        .containsExactly(tuple(1L, "TO_WATCH"), tuple(2L, null), tuple(3L, "WATCHED"));
  }

  @ParameterizedTest
  @EnumSource(SeriesStatus.class)
  void should_expose_the_watchlist_status_of_the_series_when_it_is_in_the_watchlist(
      SeriesStatus status) {
    seriesWatchlist.add(seriesInWatchlist(USER, 70523L, status));

    SearchResultPage page = service.execute(new SearchTitlesCommand(USER, "Dark", 1, "TV"));

    assertThat(page.results())
        .extracting(SearchResultItem::watchlistStatus)
        .containsExactly(status.name());
  }

  @Test
  void should_expose_no_status_when_the_series_is_not_in_the_watchlist_of_the_user() {
    seriesWatchlist.add(seriesInWatchlist(OTHER_USER, 70523L, SeriesStatus.WATCHING));

    SearchResultPage page = service.execute(new SearchTitlesCommand(USER, "Dark", 1, "TV"));

    assertThat(page.results()).extracting(SearchResultItem::watchlistStatus).containsOnlyNulls();
  }

  @Test
  void should_expose_the_status_of_each_kind_when_the_type_is_all() {
    watchlist.add(inWatchlist(USER, 603L, MovieStatus.WATCHED));
    seriesWatchlist.add(seriesInWatchlist(USER, 70523L, SeriesStatus.DROPPED));

    SearchTitlesService mixed =
        new SearchTitlesService(
            new InMemorySearchCatalog(
                MATRIX, new CatalogItem(70523L, MediaType.TV, "Matrix", null, null)),
            new InMemoryWatchlistMovieRepository(watchlist),
            new InMemoryWatchlistSeriesRepository(seriesWatchlist));

    SearchResultPage page = mixed.execute(new SearchTitlesCommand(USER, "Matrix", 1, "ALL"));

    assertThat(page.results())
        .extracting(result -> result.item().id(), SearchResultItem::watchlistStatus)
        .containsExactly(tuple(603L, "WATCHED"), tuple(70523L, "DROPPED"));
  }

  @Test
  void should_not_cross_the_statuses_of_a_movie_and_a_series_sharing_the_same_id() {
    CatalogItem movie = new CatalogItem(7L, MediaType.MOVIE, "Same", null, null);
    CatalogItem series = new CatalogItem(7L, MediaType.TV, "Same", null, null);
    SearchTitlesService both =
        new SearchTitlesService(
            new InMemorySearchCatalog(movie, series),
            new InMemoryWatchlistMovieRepository(watchlist),
            new InMemoryWatchlistSeriesRepository(seriesWatchlist));
    watchlist.add(inWatchlist(USER, 7L, MovieStatus.WATCHED));

    SearchResultPage movieOnly = both.execute(new SearchTitlesCommand(USER, "Same", 1, "ALL"));
    watchlist.clear();
    seriesWatchlist.add(seriesInWatchlist(USER, 7L, SeriesStatus.WATCHING));
    SearchResultPage seriesOnly = both.execute(new SearchTitlesCommand(USER, "Same", 1, "ALL"));

    assertThat(movieOnly.results())
        .extracting(SearchResultItem::watchlistStatus)
        .containsExactly("WATCHED", null);
    assertThat(seriesOnly.results())
        .extracting(SearchResultItem::watchlistStatus)
        .containsExactly(null, "WATCHING");
  }
}
