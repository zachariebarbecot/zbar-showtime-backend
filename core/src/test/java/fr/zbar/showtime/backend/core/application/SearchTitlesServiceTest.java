package fr.zbar.showtime.backend.core.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.zbar.showtime.backend.core.domain.CatalogItem;
import fr.zbar.showtime.backend.core.domain.MediaType;
import fr.zbar.showtime.backend.core.domain.PageNumber;
import fr.zbar.showtime.backend.core.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.domain.SearchPage;
import fr.zbar.showtime.backend.core.domain.SearchQuery;
import fr.zbar.showtime.backend.core.shared.InvalidFieldException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

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

  private final SearchTitlesService service =
      new SearchTitlesService(new InMemorySearchCatalog(MATRIX, DARK));

  @Test
  void should_search_the_catalog_with_the_trimmed_query_and_requested_page() {
    SearchPage page = service.execute(new SearchTitlesCommand("  Matrix  ", 3, "MOVIE"));

    assertThat(page.page()).isEqualTo(3);
    assertThat(page.results()).containsExactly(MATRIX);
  }

  @Test
  void should_search_the_series_when_the_type_is_tv() {
    SearchPage page = service.execute(new SearchTitlesCommand("Dark", 1, "TV"));

    assertThat(page.results()).containsExactly(DARK);
  }

  @Test
  void should_search_movies_and_series_together_when_the_type_is_all() {
    SearchTitlesService mixed =
        new SearchTitlesService(
            new InMemorySearchCatalog(
                MATRIX, new CatalogItem(1L, MediaType.TV, "Matrix", null, null)));

    SearchPage page = mixed.execute(new SearchTitlesCommand("matrix", 1, "ALL"));

    assertThat(page.results())
        .extracting(CatalogItem::type)
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
            });

    assertThatThrownBy(() -> unreachableCatalog.execute(new SearchTitlesCommand("a", 1, "MOVIE")))
        .isInstanceOf(InvalidFieldException.class);
    assertThatThrownBy(
            () -> unreachableCatalog.execute(new SearchTitlesCommand("matrix", 501, "MOVIE")))
        .isInstanceOf(InvalidFieldException.class);
    assertThatThrownBy(
            () -> unreachableCatalog.execute(new SearchTitlesCommand("matrix", 1, "BOOK")))
        .isInstanceOf(InvalidFieldException.class);
  }
}
