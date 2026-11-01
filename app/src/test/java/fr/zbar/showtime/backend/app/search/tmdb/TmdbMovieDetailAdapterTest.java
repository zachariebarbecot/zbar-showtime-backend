package fr.zbar.showtime.backend.app.search.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import fr.zbar.showtime.backend.core.search.domain.CrewMember;
import fr.zbar.showtime.backend.core.search.domain.MovieDetail;
import fr.zbar.showtime.backend.core.search.domain.MovieStatus;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
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
}
