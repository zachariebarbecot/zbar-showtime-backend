package fr.zbar.showtime.backend.app.search.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TmdbTitleAdapterTest {

  private static final TmdbProperties PROPERTIES =
      new TmdbProperties(
          "https://api.tmdb.test/3", "secret-token", "https://image.tmdb.org/t/p", "w342", "fr-FR");

  private static final String MOVIE_1_URL = "https://api.tmdb.test/3/movie/1?language=fr-FR";

  private final RestClient.Builder builder = TmdbRestClientBuilders.bootBuilder();
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

  @Test
  void should_return_the_series_when_tmdb_knows_the_id() {
    server
        .expect(requestTo("https://api.tmdb.test/3/tv/1399?language=fr-FR"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer secret-token"))
        .andRespond(
            withSuccess(
                """
                {"id":1399,"name":"GoT","poster_path":"/g.jpg","first_air_date":"2011-04-17"}
                """,
                org.springframework.http.MediaType.APPLICATION_JSON));

    CatalogItem series = adapter.findSeries(1399L);

    server.verify();
    assertThat(series)
        .isEqualTo(
            new CatalogItem(
                1399L,
                MediaType.TV,
                "GoT",
                "https://image.tmdb.org/t/p/w342/g.jpg",
                LocalDate.of(2011, 4, 17)));
  }

  @Test
  void should_throw_title_not_found_when_tmdb_answers_404() {
    server.expect(requestTo(MOVIE_1_URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

    assertThatThrownBy(() -> adapter.findMovie(1L)).isInstanceOf(TitleNotFoundException.class);
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpStatus.class,
      names = {"INTERNAL_SERVER_ERROR", "BAD_GATEWAY", "TOO_MANY_REQUESTS", "UNAUTHORIZED"})
  void should_throw_catalog_unavailable_when_tmdb_answers_an_error(HttpStatus status) {
    server.expect(requestTo(MOVIE_1_URL)).andRespond(withStatus(status));

    assertThatThrownBy(() -> adapter.findMovie(1L)).isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_throw_catalog_unavailable_when_tmdb_times_out() {
    server
        .expect(requestTo(MOVIE_1_URL))
        .andRespond(withException(new SocketTimeoutException("read timed out")));

    assertThatThrownBy(() -> adapter.findMovie(1L)).isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_throw_catalog_unavailable_when_the_release_date_is_malformed() {
    server
        .expect(requestTo(MOVIE_1_URL))
        .andRespond(
            withSuccess(
                """
                {"id":1,"title":"Matrix","release_date":"not-a-date"}
                """,
                org.springframework.http.MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> adapter.findMovie(1L)).isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_throw_catalog_unavailable_when_tmdb_answers_an_empty_body() {
    server
        .expect(requestTo(MOVIE_1_URL))
        .andRespond(withSuccess("", org.springframework.http.MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> adapter.findMovie(1L)).isInstanceOf(CatalogUnavailableException.class);
  }
}
