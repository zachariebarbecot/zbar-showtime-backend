package fr.zbar.showtime.backend.app.search.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import fr.zbar.showtime.backend.core.search.domain.CastMember;
import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.CrewMember;
import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieStatus;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TmdbMovieDetailAdapterTest {

  private static final TmdbProperties PROPERTIES =
      new TmdbProperties(
          "https://api.tmdb.test/3",
          "secret-token",
          "https://image.tmdb.org/t/p",
          "w342",
          "w185",
          "fr-FR");

  private final RestClient.Builder builder = TmdbRestClientBuilders.bootBuilder();
  private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
  private final TmdbMovieDetailAdapter adapter =
      new TmdbMovieDetailAdapter(TmdbConfiguration.authenticated(builder, PROPERTIES), PROPERTIES);

  private void respondWith(String json) {
    server
        .expect(requestTo(Matchers.startsWith("https://api.tmdb.test/3/movie/603?")))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer secret-token"))
        .andExpect(queryParam("language", "fr-FR"))
        .andExpect(queryParam("append_to_response", "credits"))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
  }

  private static String movieWithStatus(String status) {
    return """
        {"id":603,"title":"Matrix","status":%s}
        """
        .formatted(status);
  }

  @Test
  void should_map_the_identity_of_the_movie_when_tmdb_knows_the_id() {
    respondWith(
        """
        {"id":603,"title":"Matrix","original_title":"The Matrix","poster_path":"/m.jpg",
         "release_date":"1999-03-31","runtime":136,"status":"Released",
         "genres":[{"id":28,"name":"Action"},{"id":878,"name":"Science-Fiction"}],
         "overview":"Un hacker découvre la réalité.","credits":{"cast":[],"crew":[]}}
        """);

    MovieDetail movie = adapter.findMovieDetail(603L);

    server.verify();
    assertThat(movie)
        .isEqualTo(
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
  }

  @ParameterizedTest
  @CsvSource({
    "Rumored,RUMORED",
    "Planned,PLANNED",
    "In Production,IN_PRODUCTION",
    "Post Production,POST_PRODUCTION",
    "Released,RELEASED",
    "Canceled,CANCELED"
  })
  void should_map_the_tmdb_status_when_it_is_known(String tmdbStatus, MovieStatus expected) {
    respondWith(movieWithStatus("\"" + tmdbStatus + "\""));

    assertThat(adapter.findMovieDetail(603L).status()).isEqualTo(expected);
  }

  @Test
  void should_return_a_null_status_when_the_tmdb_status_is_unknown() {
    respondWith(movieWithStatus("\"Archived\""));

    assertThat(adapter.findMovieDetail(603L).status()).isNull();
  }

  @Test
  void should_list_the_directors_and_group_the_key_crew_by_person_when_the_credits_have_crew() {
    respondWith(
        """
        {"id":603,"title":"Matrix","credits":{"crew":[
          {"id":1,"name":"Lana Wachowski","job":"Director"},
          {"id":2,"name":"Lilly Wachowski","job":"Director"},
          {"id":3,"name":"Bill Pope","job":"Director of Photography"},
          {"id":4,"name":"Joel Silver","job":"Producer"},
          {"id":1,"name":"Lana Wachowski","job":"Writer"},
          {"id":5,"name":"Don Davis","job":"Composer"},
          {"id":2,"name":"Lilly Wachowski","job":"Screenplay"},
          {"id":4,"name":"Joel Silver","job":"Producer"},
          {"id":6,"name":"Grant Hill","job":"Executive Producer"}
        ]}}
        """);

    MovieDetail movie = adapter.findMovieDetail(603L);

    assertThat(movie.directors()).containsExactly("Lana Wachowski", "Lilly Wachowski");
    assertThat(movie.crew())
        .containsExactly(
            new CrewMember("Lana Wachowski", List.of("Director", "Writer")),
            new CrewMember("Lilly Wachowski", List.of("Director", "Screenplay")),
            new CrewMember("Joel Silver", List.of("Producer")),
            new CrewMember("Don Davis", List.of("Composer")));
  }

  @Test
  void should_keep_the_first_15_cast_members_in_tmdb_order_when_the_movie_has_more() {
    respondWith(
        """
        {"id":603,"title":"Matrix","credits":{"cast":[
          {"name":"Actor 1","character":"Role 1","profile_path":"/p1.jpg"},
          {"name":"Actor 2","character":"Role 2","profile_path":"/p2.jpg"},
          {"name":"Actor 3","character":"Role 3","profile_path":"/p3.jpg"},
          {"name":"Actor 4","character":"Role 4","profile_path":"/p4.jpg"},
          {"name":"Actor 5","character":"Role 5","profile_path":"/p5.jpg"},
          {"name":"Actor 6","character":"Role 6","profile_path":"/p6.jpg"},
          {"name":"Actor 7","character":"Role 7","profile_path":"/p7.jpg"},
          {"name":"Actor 8","character":"Role 8","profile_path":"/p8.jpg"},
          {"name":"Actor 9","character":"Role 9","profile_path":"/p9.jpg"},
          {"name":"Actor 10","character":"Role 10","profile_path":"/p10.jpg"},
          {"name":"Actor 11","character":"Role 11","profile_path":"/p11.jpg"},
          {"name":"Actor 12","character":"Role 12","profile_path":"/p12.jpg"},
          {"name":"Actor 13","character":"Role 13","profile_path":"/p13.jpg"},
          {"name":"Actor 14","character":"Role 14","profile_path":"/p14.jpg"},
          {"name":"Actor 15","character":"Role 15","profile_path":"/p15.jpg"},
          {"name":"Actor 16","character":"Role 16","profile_path":"/p16.jpg"}
        ]}}
        """);

    MovieDetail movie = adapter.findMovieDetail(603L);

    assertThat(movie.cast()).hasSize(15);
    assertThat(movie.cast().getFirst())
        .isEqualTo(new CastMember("Actor 1", "Role 1", "https://image.tmdb.org/t/p/w185/p1.jpg"));
    assertThat(movie.cast().getLast().name()).isEqualTo("Actor 15");
  }

  @Test
  void should_return_a_null_photo_when_a_cast_member_has_no_profile() {
    respondWith(
        """
        {"id":603,"title":"Matrix","credits":{"cast":[{"name":"Keanu Reeves","character":"Neo"}]}}
        """);

    assertThat(adapter.findMovieDetail(603L).cast())
        .containsExactly(new CastMember("Keanu Reeves", "Neo", null));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        """
        {"id":603,"title":"Matrix","runtime":0,"release_date":"","overview":""}
        """,
        """
        {"id":603,"title":"Matrix","release_date":null,"overview":null,"credits":{}}
        """
      })
  void should_return_null_fields_and_empty_lists_when_tmdb_has_no_data(String json) {
    respondWith(json);

    MovieDetail movie = adapter.findMovieDetail(603L);

    assertThat(movie.runtime()).isNull();
    assertThat(movie.releaseDate()).isNull();
    assertThat(movie.overview()).isNull();
    assertThat(movie.posterUrl()).isNull();
    assertThat(movie.genres()).isEmpty();
    assertThat(movie.directors()).isEmpty();
    assertThat(movie.crew()).isEmpty();
    assertThat(movie.cast()).isEmpty();
  }

  @Test
  void should_throw_title_not_found_when_tmdb_answers_404() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/movie/603?")))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    assertThatThrownBy(() -> adapter.findMovieDetail(603L))
        .isInstanceOf(TitleNotFoundException.class);
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpStatus.class,
      names = {"INTERNAL_SERVER_ERROR", "BAD_GATEWAY", "TOO_MANY_REQUESTS", "UNAUTHORIZED"})
  void should_throw_catalog_unavailable_when_tmdb_answers_an_error(HttpStatus status) {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/movie/603?")))
        .andRespond(withStatus(status));

    assertThatThrownBy(() -> adapter.findMovieDetail(603L))
        .isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_throw_catalog_unavailable_when_tmdb_times_out() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/movie/603?")))
        .andRespond(withException(new SocketTimeoutException("read timed out")));

    assertThatThrownBy(() -> adapter.findMovieDetail(603L))
        .isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_throw_catalog_unavailable_when_tmdb_answers_an_empty_body() {
    respondWith("");

    assertThatThrownBy(() -> adapter.findMovieDetail(603L))
        .isInstanceOf(CatalogUnavailableException.class);
  }
}
