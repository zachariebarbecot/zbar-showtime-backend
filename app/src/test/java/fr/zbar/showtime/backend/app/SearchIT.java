package fr.zbar.showtime.backend.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.core.domain.CatalogItem;
import fr.zbar.showtime.backend.core.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.domain.MediaType;
import fr.zbar.showtime.backend.core.domain.PageNumber;
import fr.zbar.showtime.backend.core.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.domain.SearchPage;
import fr.zbar.showtime.backend.core.domain.SearchQuery;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class SearchIT extends AbstractIT {

  @MockitoBean private SearchCatalog searchCatalog;

  @Autowired private JwtEncoder jwtEncoder;

  private String newUserEmail() {
    String email = "search-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    return email;
  }

  private RestTestClient.ResponseSpec search(String email, String uri, Object... uriVariables) {
    return restTestClient
        .get()
        .uri(uri, uriVariables)
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

    search("search-ca1@mail.fr", "/search?query={query}&type=MOVIE", " L'été & co ")
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
  void should_search_movies_and_series_together_when_no_type_is_given() {
    String email = newUserEmail();
    when(searchCatalog.searchAll(new SearchQuery("star"), new PageNumber(1)))
        .thenReturn(
            new SearchPage(
                1,
                1,
                2,
                List.of(
                    new CatalogItem(1L, MediaType.TV, "Star Trek", null, null),
                    new CatalogItem(11L, MediaType.MOVIE, "Star Wars", null, null))));

    search(email, "/search?query=star")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.results.length()")
        .isEqualTo(2)
        .jsonPath("$.results[0].type")
        .isEqualTo("TV")
        .jsonPath("$.results[1].type")
        .isEqualTo("MOVIE");
  }

  @Test
  void should_search_like_without_type_when_the_type_is_all() {
    String email = newUserEmail();
    when(searchCatalog.searchAll(new SearchQuery("star"), new PageNumber(1)))
        .thenReturn(
            new SearchPage(
                1, 1, 1, List.of(new CatalogItem(11L, MediaType.MOVIE, "Star Wars", null, null))));

    search(email, "/search?query=star&type=ALL")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.results[0].title")
        .isEqualTo("Star Wars");
  }

  @Test
  void should_return_200_with_the_series_page_when_searching_with_type_tv() {
    String email = newUserEmail();
    when(searchCatalog.searchSeries(new SearchQuery("dark"), new PageNumber(2)))
        .thenReturn(
            new SearchPage(
                2,
                3,
                45,
                List.of(
                    new CatalogItem(
                        70523L,
                        MediaType.TV,
                        "Dark",
                        "https://image.tmdb.org/t/p/w342/d.jpg",
                        LocalDate.of(2017, 12, 1)))));

    search(email, "/search?query=dark&type=TV&page=2")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.page")
        .isEqualTo(2)
        .jsonPath("$.results[0].type")
        .isEqualTo("TV")
        .jsonPath("$.results[0].title")
        .isEqualTo("Dark")
        .jsonPath("$.results[0].releaseDate")
        .isEqualTo("2017-12-01");
  }

  @Test
  void should_return_401_and_not_call_tmdb_for_series_when_there_is_no_token() {
    restTestClient
        .get()
        .uri("/search?query=dark&type=TV")
        .exchange()
        .expectStatus()
        .isUnauthorized();

    verifyNoInteractions(searchCatalog);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"query=a", "query=dark&page=0", "query=dark&page=501", "query=dark&page=x"})
  void should_return_400_and_not_call_tmdb_for_series_when_query_or_page_is_invalid(String params) {
    String email = newUserEmail();

    search(email, "/search?type=TV&" + params).expectStatus().isBadRequest();

    verifyNoInteractions(searchCatalog);
  }

  @Test
  void should_return_200_with_an_empty_series_list_when_the_page_is_beyond_the_last_one() {
    String email = newUserEmail();
    when(searchCatalog.searchSeries(new SearchQuery("dark"), new PageNumber(9)))
        .thenReturn(new SearchPage(9, 3, 50, List.of()));

    search(email, "/search?query=dark&type=TV&page=9")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.results.length()")
        .isEqualTo(0)
        .jsonPath("$.totalPages")
        .isEqualTo(3);
  }

  @ParameterizedTest
  @ValueSource(strings = {"BOOK", "movie", "tv", "", "all"})
  void should_return_400_on_type_and_not_call_tmdb_when_the_type_is_not_accepted(String type) {
    String email = newUserEmail();

    search(email, "/search?query=matrix&type={type}", type)
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("type")
        .jsonPath("$.errors[0].message")
        .isEqualTo("Type must be MOVIE, TV or ALL");

    verifyNoInteractions(searchCatalog);
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

  @Test
  void should_return_401_and_not_call_tmdb_when_there_is_no_token() {
    restTestClient
        .get()
        .uri("/search?query=matrix&type=MOVIE")
        .exchange()
        .expectStatus()
        .isUnauthorized();

    verifyNoInteractions(searchCatalog);
  }

  @Test
  void should_return_401_and_not_call_tmdb_when_the_token_is_invalid() {
    restTestClient
        .get()
        .uri("/search?query=matrix&type=MOVIE")
        .header(HttpHeaders.AUTHORIZATION, "Bearer not.a.jwt")
        .exchange()
        .expectStatus()
        .isUnauthorized();

    verifyNoInteractions(searchCatalog);
  }

  @Test
  void should_return_401_and_not_call_tmdb_when_the_token_is_expired() {
    Instant issuedAt = FixedClockConfiguration.NOW.minus(Duration.ofHours(3));
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(UUID.randomUUID().toString())
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plus(Duration.ofHours(1)))
            .build();
    String expired =
        jwtEncoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();

    restTestClient
        .get()
        .uri("/search?query=matrix&type=MOVIE")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expired)
        .exchange()
        .expectStatus()
        .isUnauthorized();

    verifyNoInteractions(searchCatalog);
  }

  @Test
  void should_return_400_on_query_and_not_call_tmdb_when_the_query_is_absent() {
    registerUser("search-ca3-absent@mail.fr");

    search("search-ca3-absent@mail.fr", "/search?type=MOVIE")
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("query")
        .jsonPath("$.errors[0].message")
        .isEqualTo("Query must be between 2 and 100 characters");

    verifyNoInteractions(searchCatalog);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "a", " a "})
  void should_return_400_on_query_and_not_call_tmdb_when_the_query_is_too_short(String query) {
    String email = newUserEmail();

    search(email, "/search?query={query}&type=MOVIE", query)
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("query");

    verifyNoInteractions(searchCatalog);
  }

  @Test
  void should_return_400_on_query_and_not_call_tmdb_when_the_query_is_too_long() {
    registerUser("search-ca3-long@mail.fr");

    search("search-ca3-long@mail.fr", "/search?query={query}", "a".repeat(101))
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("query");

    verifyNoInteractions(searchCatalog);
  }

  @ParameterizedTest
  @ValueSource(strings = {"abc", "1.5", "0", "-1", "501"})
  void should_return_400_on_page_and_not_call_tmdb_when_the_page_is_invalid(String page) {
    String email = newUserEmail();

    search(email, "/search?query=matrix&page={page}", page)
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("page")
        .jsonPath("$.errors[0].message")
        .isNotEmpty();

    verifyNoInteractions(searchCatalog);
  }

  @Test
  void should_return_null_poster_and_date_and_keep_the_item_when_they_are_unknown() {
    String email = newUserEmail();
    when(searchCatalog.searchMovies(new SearchQuery("matrix"), new PageNumber(1)))
        .thenReturn(
            new SearchPage(
                1, 1, 1, List.of(new CatalogItem(7L, MediaType.MOVIE, "Matrix", null, null))));

    search(email, "/search?query=matrix&type=MOVIE")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.results.length()")
        .isEqualTo(1)
        .jsonPath("$.results[0].title")
        .isEqualTo("Matrix")
        .jsonPath("$.results[0].posterUrl")
        .value(value -> assertThat(value).isNull())
        .jsonPath("$.results[0].releaseDate")
        .value(value -> assertThat(value).isNull());
  }

  @Test
  void should_return_200_with_an_empty_list_when_there_is_no_result() {
    String email = newUserEmail();
    when(searchCatalog.searchMovies(new SearchQuery("zzzz"), new PageNumber(1)))
        .thenReturn(new SearchPage(1, 0, 0, List.of()));

    search(email, "/search?query=zzzz&type=MOVIE")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.results.length()")
        .isEqualTo(0)
        .jsonPath("$.totalResults")
        .isEqualTo(0);
  }

  @Test
  void should_return_200_with_an_empty_list_when_the_page_is_beyond_the_last_one() {
    String email = newUserEmail();
    when(searchCatalog.searchMovies(new SearchQuery("matrix"), new PageNumber(500)))
        .thenReturn(new SearchPage(500, 3, 50, List.of()));

    search(email, "/search?query=matrix&page=500&type=MOVIE")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.results.length()")
        .isEqualTo(0)
        .jsonPath("$.totalPages")
        .isEqualTo(3);
  }

  @Test
  void should_return_502_with_a_generic_problem_when_the_catalog_is_unavailable() {
    String email = newUserEmail();
    when(searchCatalog.searchMovies(new SearchQuery("matrix"), new PageNumber(1)))
        .thenThrow(
            new CatalogUnavailableException(
                new IllegalStateException(
                    "401 from https://api.themoviedb.org/3 token test-tmdb-token")));

    search(email, "/search?query=matrix&type=MOVIE")
        .expectStatus()
        .isEqualTo(502)
        .expectHeader()
        .contentTypeCompatibleWith("application/problem+json")
        .expectBody(String.class)
        .value(
            body -> {
              assertThat(body).contains("\"status\":502");
              assertThat(body)
                  .doesNotContain("test-tmdb-token", "themoviedb", "tmdb.org", "Exception");
            });
  }

  @Test
  void should_return_502_with_a_generic_problem_when_the_series_catalog_is_unavailable() {
    String email = newUserEmail();
    when(searchCatalog.searchSeries(new SearchQuery("dark"), new PageNumber(1)))
        .thenThrow(
            new CatalogUnavailableException(
                new IllegalStateException("429 from https://api.themoviedb.org/3")));

    search(email, "/search?query=dark&type=TV")
        .expectStatus()
        .isEqualTo(502)
        .expectBody(String.class)
        .value(body -> assertThat(body).contains("\"status\":502").doesNotContain("themoviedb"));
  }
}
