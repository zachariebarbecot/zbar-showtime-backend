package fr.zbar.showtime.backend.app.search.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TmdbTitleAdapterTest {

  private static final TmdbProperties PROPERTIES =
      new TmdbProperties(
          "https://api.tmdb.test/3",
          "secret-token",
          "https://image.tmdb.org/t/p",
          "w342",
          "fr-FR",
          Duration.ofSeconds(1),
          Duration.ofSeconds(2));

  private final RestClient.Builder builder = RestClient.builder();
  private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
  private final TmdbTitleAdapter adapter =
      new TmdbTitleAdapter(TmdbConfiguration.authenticated(builder, PROPERTIES), PROPERTIES);

  @Test
  void should_return_the_movie_when_tmdb_knows_the_id() {
    server
        .expect(requestTo("https://api.tmdb.test/3/movie/603?language=fr-FR"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer secret-token"))
        .andRespond(
            withSuccess(
                """
                {"id":603,"title":"Matrix","poster_path":"/m.jpg","release_date":"1999-03-31"}
                """,
                org.springframework.http.MediaType.APPLICATION_JSON));

    CatalogItem movie = adapter.findMovie(603L);

    server.verify();
    assertThat(movie)
        .isEqualTo(
            new CatalogItem(
                603L,
                MediaType.MOVIE,
                "Matrix",
                "https://image.tmdb.org/t/p/w342/m.jpg",
                LocalDate.of(1999, 3, 31)));
  }
}
