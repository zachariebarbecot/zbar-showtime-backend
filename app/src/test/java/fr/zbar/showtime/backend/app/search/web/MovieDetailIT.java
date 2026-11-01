package fr.zbar.showtime.backend.app.search.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.search.domain.CastMember;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.CrewMember;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.MovieStatus;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class MovieDetailIT extends AbstractIT {

  @MockitoBean private MovieDetailCatalog movieDetailCatalog;

  @MockitoBean private TitleCatalog titleCatalog;

  private RestTestClient.ResponseSpec detail(String email, String id) {
    return restTestClient
        .get()
        .uri("/movies/{id}", id)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange();
  }

  @Test
  void should_return_200_with_the_movie_detail_when_tmdb_knows_the_movie() {
    registerUser("movie-ca1@mail.fr");
    when(movieDetailCatalog.findMovieDetail(603L))
        .thenReturn(
            new MovieDetail(
                603L,
                "Matrix",
                "The Matrix",
                List.of("Action", "Science-Fiction"),
                LocalDate.of(1999, 3, 31),
                136,
                MovieStatus.RELEASED,
                "Un hacker découvre la réalité.",
                "https://image.tmdb.org/t/p/w342/m.jpg",
                List.of(),
                List.of(),
                List.of()));

    detail("movie-ca1@mail.fr", "603")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.id")
        .isEqualTo(603)
        .jsonPath("$.title")
        .isEqualTo("Matrix")
        .jsonPath("$.originalTitle")
        .isEqualTo("The Matrix")
        .jsonPath("$.posterUrl")
        .isEqualTo("https://image.tmdb.org/t/p/w342/m.jpg")
        .jsonPath("$.releaseDate")
        .isEqualTo("1999-03-31")
        .jsonPath("$.runtime")
        .isEqualTo(136)
        .jsonPath("$.genres[1]")
        .isEqualTo("Science-Fiction")
        .jsonPath("$.overview")
        .isEqualTo("Un hacker découvre la réalité.")
        .jsonPath("$.status")
        .isEqualTo("RELEASED");
  }

  @Test
  void should_return_401_when_there_is_no_token() {
    restTestClient.get().uri("/movies/603").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void should_return_the_directors_and_the_key_crew_when_the_movie_has_a_crew() {
    registerUser("movie-ca2@mail.fr");
    when(movieDetailCatalog.findMovieDetail(603L))
        .thenReturn(
            new MovieDetail(
                603L,
                "Matrix",
                "The Matrix",
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of("Lana Wachowski"),
                List.of(new CrewMember("Lana Wachowski", List.of("Director", "Writer"))),
                List.of()));

    detail("movie-ca2@mail.fr", "603")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.directors[0]")
        .isEqualTo("Lana Wachowski")
        .jsonPath("$.crew[0].name")
        .isEqualTo("Lana Wachowski")
        .jsonPath("$.crew[0].jobs[1]")
        .isEqualTo("Writer");
  }

  @Test
  void should_return_the_cast_when_the_movie_has_actors() {
    registerUser("movie-ca3@mail.fr");
    when(movieDetailCatalog.findMovieDetail(603L))
        .thenReturn(
            new MovieDetail(
                603L,
                "Matrix",
                "The Matrix",
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(
                    new CastMember(
                        "Keanu Reeves", "Neo", "https://image.tmdb.org/t/p/w185/k.jpg"))));

    detail("movie-ca3@mail.fr", "603")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.cast[0].name")
        .isEqualTo("Keanu Reeves")
        .jsonPath("$.cast[0].character")
        .isEqualTo("Neo")
        .jsonPath("$.cast[0].photoUrl")
        .isEqualTo("https://image.tmdb.org/t/p/w185/k.jpg");
  }

  @Test
  void should_return_200_with_null_fields_and_empty_lists_when_the_movie_has_no_data() {
    registerUser("movie-ca4@mail.fr");
    when(movieDetailCatalog.findMovieDetail(603L))
        .thenReturn(
            new MovieDetail(
                603L,
                "Matrix",
                "The Matrix",
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of()));

    detail("movie-ca4@mail.fr", "603")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.runtime")
        .isEqualTo(null)
        .jsonPath("$.releaseDate")
        .isEqualTo(null)
        .jsonPath("$.overview")
        .isEqualTo(null)
        .jsonPath("$.directors")
        .isEmpty()
        .jsonPath("$.crew")
        .isEmpty()
        .jsonPath("$.cast")
        .isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "-1", "abc"})
  void should_return_400_without_calling_tmdb_when_the_id_is_invalid(String id) {
    String email = "movie-ca5a-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);

    detail(email, id)
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentTypeCompatibleWith("application/problem+json")
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("tmdbId");

    verifyNoInteractions(movieDetailCatalog);
  }

  @Test
  void should_return_404_when_tmdb_does_not_know_the_movie() {
    registerUser("movie-ca5b@mail.fr");
    when(movieDetailCatalog.findMovieDetail(999L)).thenThrow(new TitleNotFoundException());

    detail("movie-ca5b@mail.fr", "999")
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentTypeCompatibleWith("application/problem+json");
  }

  @Test
  void should_return_502_without_technical_detail_when_the_catalog_is_unavailable() {
    registerUser("movie-ca5c@mail.fr");
    when(movieDetailCatalog.findMovieDetail(603L))
        .thenThrow(
            new CatalogUnavailableException(
                new IllegalStateException(
                    "401 from https://api.themoviedb.org/3 test-tmdb-token")));

    detail("movie-ca5c@mail.fr", "603")
        .expectStatus()
        .isEqualTo(502)
        .expectHeader()
        .contentTypeCompatibleWith("application/problem+json")
        .expectBody(String.class)
        .value(
            body ->
                assertThat(body)
                    .contains("\"status\":502")
                    .doesNotContain("test-tmdb-token", "themoviedb", "Exception"));
  }

  private static MovieDetail matrix() {
    return new MovieDetail(
        603L,
        "Matrix",
        "The Matrix",
        List.of(),
        null,
        null,
        null,
        null,
        null,
        List.of(),
        List.of(),
        List.of());
  }

  private void addMovieToWatchlist(String email) {
    when(titleCatalog.findMovie(603L))
        .thenReturn(new CatalogItem(603L, MediaType.MOVIE, "Matrix", null, null));
    restTestClient
        .post()
        .uri("/watchlist/movies")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", 603L))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  private void changeMovieStatus(String email, String status) {
    restTestClient
        .patch()
        .uri("/watchlist/movies/{tmdbId}", 603L)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("status", status))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void should_expose_the_watchlist_status_when_the_movie_is_in_my_watchlist() {
    String email = "movie-us2-ca1-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    when(movieDetailCatalog.findMovieDetail(603L)).thenReturn(matrix());
    addMovieToWatchlist(email);

    detail(email, "603")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.watchlistStatus")
        .isEqualTo("TO_WATCH");
  }

  @Test
  void should_expose_a_null_watchlist_status_and_the_rest_of_the_detail_when_not_in_my_watchlist() {
    String email = "movie-us2-ca2-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    when(movieDetailCatalog.findMovieDetail(603L)).thenReturn(matrix());

    detail(email, "603")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.watchlistStatus")
        .isEqualTo(null)
        .jsonPath("$.title")
        .isEqualTo("Matrix");
  }

  @Test
  void should_expose_a_null_watchlist_status_when_only_another_user_has_the_movie() {
    String owner = "movie-us2-ca3-owner-" + UUID.randomUUID() + "@mail.fr";
    String other = "movie-us2-ca3-other-" + UUID.randomUUID() + "@mail.fr";
    registerUser(owner);
    registerUser(other);
    when(movieDetailCatalog.findMovieDetail(603L)).thenReturn(matrix());
    addMovieToWatchlist(owner);

    detail(other, "603").expectStatus().isOk().expectBody().jsonPath("$.watchlistStatus").isEqualTo(null);
  }

  @Test
  void should_expose_the_new_watchlist_status_when_it_was_changed() {
    String email = "movie-us2-ca4-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    when(movieDetailCatalog.findMovieDetail(603L)).thenReturn(matrix());
    addMovieToWatchlist(email);
    changeMovieStatus(email, "WATCHED");

    detail(email, "603")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.watchlistStatus")
        .isEqualTo("WATCHED");
  }
}
