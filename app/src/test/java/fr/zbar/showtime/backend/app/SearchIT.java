package fr.zbar.showtime.backend.app;

import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.core.domain.CatalogItem;
import fr.zbar.showtime.backend.core.domain.MediaType;
import fr.zbar.showtime.backend.core.domain.PageNumber;
import fr.zbar.showtime.backend.core.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.domain.SearchPage;
import fr.zbar.showtime.backend.core.domain.SearchQuery;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

class SearchIT extends AbstractIT {

  @MockitoBean private SearchCatalog searchCatalog;

  private RestTestClient.ResponseSpec search(String email, String uri) {
    return restTestClient
        .get()
        .uri(uri)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange();
  }

  @Test
  void should_return_200_with_the_movies_page_when_searching_with_type_movie() {
    registerUser("search-ca1@mail.fr");
    when(searchCatalog.searchMovies(new SearchQuery("L'été & co"), new PageNumber(1)))
        .thenReturn(
            new SearchPage(
                1,
                4,
                70,
                List.of(
                    new CatalogItem(
                        603L,
                        MediaType.MOVIE,
                        "Matrix",
                        "https://image.tmdb.org/t/p/w342/m.jpg",
                        LocalDate.of(1999, 3, 31)))));

    search("search-ca1@mail.fr", "/search?query=%20L'%C3%A9t%C3%A9%20%26%20co%20&type=MOVIE")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.page")
        .isEqualTo(1)
        .jsonPath("$.totalPages")
        .isEqualTo(4)
        .jsonPath("$.totalResults")
        .isEqualTo(70)
        .jsonPath("$.results.length()")
        .isEqualTo(1)
        .jsonPath("$.results[0].id")
        .isEqualTo(603)
        .jsonPath("$.results[0].type")
        .isEqualTo("MOVIE")
        .jsonPath("$.results[0].title")
        .isEqualTo("Matrix")
        .jsonPath("$.results[0].posterUrl")
        .isEqualTo("https://image.tmdb.org/t/p/w342/m.jpg")
        .jsonPath("$.results[0].releaseDate")
        .isEqualTo("1999-03-31");
  }

  @Test
  void should_forward_the_requested_page_when_page_is_given() {
    registerUser("search-ca1-page@mail.fr");
    when(searchCatalog.searchMovies(new SearchQuery("matrix"), new PageNumber(3)))
        .thenReturn(new SearchPage(3, 4, 70, List.of()));

    search("search-ca1-page@mail.fr", "/search?query=matrix&type=MOVIE&page=3")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.page")
        .isEqualTo(3);
  }
}
