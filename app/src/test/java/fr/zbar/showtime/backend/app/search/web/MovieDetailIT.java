package fr.zbar.showtime.backend.app.search.web;

import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.MovieStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class MovieDetailIT extends AbstractIT {

  @MockitoBean private MovieDetailCatalog movieDetailCatalog;

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
}
