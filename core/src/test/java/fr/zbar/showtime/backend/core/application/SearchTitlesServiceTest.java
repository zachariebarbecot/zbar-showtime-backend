package fr.zbar.showtime.backend.core.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.core.domain.CatalogItem;
import fr.zbar.showtime.backend.core.domain.MediaType;
import fr.zbar.showtime.backend.core.domain.SearchPage;
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

  private final SearchTitlesService service =
      new SearchTitlesService(new InMemorySearchCatalog(MATRIX));

  @Test
  void should_search_the_catalog_with_the_trimmed_query_and_requested_page() {
    SearchPage page = service.execute(new SearchTitlesCommand("  Matrix  ", 3));

    assertThat(page.page()).isEqualTo(3);
    assertThat(page.results()).containsExactly(MATRIX);
  }
}
