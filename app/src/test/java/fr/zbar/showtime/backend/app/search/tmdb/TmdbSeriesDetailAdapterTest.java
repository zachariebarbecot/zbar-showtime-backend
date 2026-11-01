package fr.zbar.showtime.backend.app.search.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import fr.zbar.showtime.backend.core.search.domain.CrewMember;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TmdbSeriesDetailAdapterTest {

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
  private final TmdbSeriesDetailAdapter adapter =
      new TmdbSeriesDetailAdapter(
          TmdbConfiguration.seriesApi(TmdbConfiguration.authenticated(builder, PROPERTIES)),
          PROPERTIES);

  private void respondWith(String json) {
    server
        .expect(requestTo(Matchers.startsWith("https://api.tmdb.test/3/tv/1396?")))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer secret-token"))
        .andExpect(queryParam("language", "fr-FR"))
        .andExpect(queryParam("append_to_response", "aggregate_credits"))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
  }

  @Test
  void should_map_the_identity_of_the_series_when_tmdb_knows_the_id() {
    respondWith(
        """
        {"id":1396,"name":"Breaking Bad","original_name":"Breaking Bad",
         "poster_path":"/b.jpg","first_air_date":"2008-01-20","last_air_date":"2013-09-29",
         "number_of_seasons":5,"number_of_episodes":62,"status":"Ended",
         "genres":[{"id":18,"name":"Drame"},{"id":80,"name":"Crime"}],
         "overview":"Un professeur de chimie devient fabricant de drogue."}
        """);

    SeriesDetail series = adapter.findSeriesDetail(1396L);

    server.verify();
    assertThat(series)
        .isEqualTo(
            new SeriesDetail(
                1396L,
                "Breaking Bad",
                "Breaking Bad",
                List.of("Drame", "Crime"),
                LocalDate.of(2008, 1, 20),
                LocalDate.of(2013, 9, 29),
                SeriesStatus.ENDED,
                "Un professeur de chimie devient fabricant de drogue.",
                "https://image.tmdb.org/t/p/w342/b.jpg",
                5,
                62,
                List.of(),
                List.of(),
                List.of()));
  }

  @ParameterizedTest
  @CsvSource({
    "Returning Series,RETURNING",
    "Ended,ENDED",
    "Canceled,CANCELED",
    "In Production,IN_PRODUCTION",
    "Planned,PLANNED",
    "Pilot,PILOT"
  })
  void should_map_the_tmdb_status_when_it_is_known(String tmdbStatus, SeriesStatus expected) {
    respondWith("{\"id\":1396,\"name\":\"Breaking Bad\",\"status\":\"" + tmdbStatus + "\"}");

    assertThat(adapter.findSeriesDetail(1396L).status()).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        """
        {"id":1396,"name":"Breaking Bad","status":"Archived"}
        """,
        """
        {"id":1396,"name":"Breaking Bad","status":null}
        """
      })
  void should_return_a_null_status_when_the_tmdb_status_is_unknown_or_absent(String json) {
    respondWith(json);

    assertThat(adapter.findSeriesDetail(1396L).status()).isNull();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        """
        {"id":1396,"name":"Breaking Bad","first_air_date":"","last_air_date":"","overview":""}
        """,
        """
        {"id":1396,"name":"Breaking Bad","first_air_date":null,"last_air_date":null,
         "overview":null,"genres":null}
        """
      })
  void should_return_null_fields_and_empty_lists_when_tmdb_has_no_data(String json) {
    respondWith(json);

    SeriesDetail series = adapter.findSeriesDetail(1396L);

    assertThat(series.firstAirDate()).isNull();
    assertThat(series.lastAirDate()).isNull();
    assertThat(series.overview()).isNull();
    assertThat(series.posterUrl()).isNull();
    assertThat(series.genres()).isEmpty();
    assertThat(series.creators()).isEmpty();
    assertThat(series.crew()).isEmpty();
    assertThat(series.cast()).isEmpty();
  }

  @Test
  void should_list_the_creators_and_group_the_key_crew_by_person_when_the_credits_have_crew() {
    respondWith(
        """
        {"id":1396,"name":"Breaking Bad","created_by":[{"name":"Vince Gilligan"},{"name":"Peter Gould"}],
         "aggregate_credits":{"crew":[
          {"id":1,"name":"Vince Gilligan","jobs":[{"job":"Writer"},{"job":"Executive Producer"}]},
          {"id":2,"name":"Michael Slovis","jobs":[{"job":"Director of Photography"}]},
          {"id":3,"name":"Dave Porter","jobs":[{"job":"Composer"}]},
          {"id":1,"name":"Vince Gilligan","jobs":[{"job":"Producer"},{"job":"Writer"}]},
          {"id":4,"name":"Peter Gould","jobs":[{"job":"Screenplay"}]}
        ]}}
        """);

    SeriesDetail series = adapter.findSeriesDetail(1396L);

    assertThat(series.creators()).containsExactly("Vince Gilligan", "Peter Gould");
    assertThat(series.crew())
        .containsExactly(
            new CrewMember("Vince Gilligan", List.of("Writer", "Producer")),
            new CrewMember("Dave Porter", List.of("Composer")),
            new CrewMember("Peter Gould", List.of("Screenplay")));
  }
}
