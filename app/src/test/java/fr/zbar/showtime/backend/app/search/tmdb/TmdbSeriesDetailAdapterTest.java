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
import fr.zbar.showtime.backend.core.search.domain.SeasonSummary;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
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

class TmdbSeriesDetailAdapterTest {

  private static final TmdbProperties PROPERTIES =
      new TmdbProperties(
          "https://api.tmdb.test/3",
          "secret-token",
          "https://image.tmdb.org/t/p",
          "w342",
          "w185",
          "w300",
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

  @Test
  void should_keep_the_first_15_cast_members_in_tmdb_order_when_the_series_has_more() {
    respondWith(
        """
        {"id":1396,"name":"Breaking Bad","aggregate_credits":{"cast":[
          {"name":"Actor 1","roles":[{"character":"Role 1"}],"profile_path":"/p1.jpg"},
          {"name":"Actor 2","roles":[{"character":"Role 2"}],"profile_path":"/p2.jpg"},
          {"name":"Actor 3","roles":[{"character":"Role 3"}],"profile_path":"/p3.jpg"},
          {"name":"Actor 4","roles":[{"character":"Role 4"}],"profile_path":"/p4.jpg"},
          {"name":"Actor 5","roles":[{"character":"Role 5"}],"profile_path":"/p5.jpg"},
          {"name":"Actor 6","roles":[{"character":"Role 6"}],"profile_path":"/p6.jpg"},
          {"name":"Actor 7","roles":[{"character":"Role 7"}],"profile_path":"/p7.jpg"},
          {"name":"Actor 8","roles":[{"character":"Role 8"}],"profile_path":"/p8.jpg"},
          {"name":"Actor 9","roles":[{"character":"Role 9"}],"profile_path":"/p9.jpg"},
          {"name":"Actor 10","roles":[{"character":"Role 10"}],"profile_path":"/p10.jpg"},
          {"name":"Actor 11","roles":[{"character":"Role 11"}],"profile_path":"/p11.jpg"},
          {"name":"Actor 12","roles":[{"character":"Role 12"}],"profile_path":"/p12.jpg"},
          {"name":"Actor 13","roles":[{"character":"Role 13"}],"profile_path":"/p13.jpg"},
          {"name":"Actor 14","roles":[{"character":"Role 14"}],"profile_path":"/p14.jpg"},
          {"name":"Actor 15","roles":[{"character":"Role 15"}],"profile_path":"/p15.jpg"},
          {"name":"Actor 16","roles":[{"character":"Role 16"}],"profile_path":"/p16.jpg"}
        ]}}
        """);

    SeriesDetail series = adapter.findSeriesDetail(1396L);

    assertThat(series.cast()).hasSize(15);
    assertThat(series.cast().getFirst())
        .isEqualTo(new CastMember("Actor 1", "Role 1", "https://image.tmdb.org/t/p/w185/p1.jpg"));
    assertThat(series.cast().getLast().name()).isEqualTo("Actor 15");
  }

  @Test
  void should_return_a_null_photo_when_a_cast_member_has_no_profile() {
    respondWith(
        """
        {"id":1396,"name":"Breaking Bad","aggregate_credits":{"cast":[
          {"name":"Bryan Cranston","roles":[{"character":"Walter White"}]}]}}
        """);

    assertThat(adapter.findSeriesDetail(1396L).cast())
        .containsExactly(new CastMember("Bryan Cranston", "Walter White", null));
  }

  @Test
  void should_list_the_seasons_in_tmdb_order_with_the_specials_season_at_its_position() {
    respondWith(
        """
        {"id":1396,"name":"Breaking Bad","seasons":[
          {"season_number":0,"name":"Épisodes spéciaux","episode_count":2,
           "air_date":"2009-02-17","poster_path":"/s0.jpg"},
          {"season_number":1,"name":"Saison 1","episode_count":7,
           "air_date":"2008-01-20","poster_path":"/s1.jpg"}]}
        """);

    assertThat(adapter.findSeriesDetail(1396L).seasons())
        .containsExactly(
            new SeasonSummary(
                0,
                "Épisodes spéciaux",
                2,
                LocalDate.of(2009, 2, 17),
                "https://image.tmdb.org/t/p/w342/s0.jpg"),
            new SeasonSummary(
                1,
                "Saison 1",
                7,
                LocalDate.of(2008, 1, 20),
                "https://image.tmdb.org/t/p/w342/s1.jpg"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        """
        {"id":1396,"name":"Breaking Bad"}
        """,
        """
        {"id":1396,"name":"Breaking Bad","seasons":null}
        """,
        """
        {"id":1396,"name":"Breaking Bad","seasons":[]}
        """
      })
  void should_return_no_seasons_when_tmdb_lists_none(String json) {
    respondWith(json);

    assertThat(adapter.findSeriesDetail(1396L).seasons()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        """
        {"id":1396,"name":"Breaking Bad","seasons":[
          {"season_number":3,"name":"Saison 3","episode_count":0,"air_date":"","poster_path":null}]}
        """,
        """
        {"id":1396,"name":"Breaking Bad","seasons":[
          {"season_number":3,"name":"Saison 3","episode_count":0}]}
        """
      })
  void should_keep_a_season_with_a_null_date_and_poster_when_tmdb_has_none(String json) {
    respondWith(json);

    assertThat(adapter.findSeriesDetail(1396L).seasons())
        .containsExactly(new SeasonSummary(3, "Saison 3", 0, null, null));
  }

  @Test
  void should_throw_title_not_found_when_tmdb_answers_404() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/tv/1396?")))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    assertThatThrownBy(() -> adapter.findSeriesDetail(1396L))
        .isInstanceOf(TitleNotFoundException.class);
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpStatus.class,
      names = {"INTERNAL_SERVER_ERROR", "BAD_GATEWAY", "TOO_MANY_REQUESTS", "UNAUTHORIZED"})
  void should_throw_catalog_unavailable_when_tmdb_answers_an_error(HttpStatus status) {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/tv/1396?")))
        .andRespond(withStatus(status));

    assertThatThrownBy(() -> adapter.findSeriesDetail(1396L))
        .isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_throw_catalog_unavailable_when_tmdb_times_out() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/tv/1396?")))
        .andRespond(withException(new SocketTimeoutException("read timed out")));

    assertThatThrownBy(() -> adapter.findSeriesDetail(1396L))
        .isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_throw_catalog_unavailable_when_tmdb_answers_an_empty_body() {
    respondWith("");

    assertThatThrownBy(() -> adapter.findSeriesDetail(1396L))
        .isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_tolerate_a_cast_member_without_roles_and_a_crew_member_without_jobs() {
    respondWith(
        """
        {"id":1396,"name":"Breaking Bad","aggregate_credits":{
          "cast":[{"name":"A","roles":[]},{"name":"B"}],
          "crew":[{"id":1,"name":"No Jobs"},{"id":2,"name":"Dave Porter","jobs":[{"job":"Composer"}]}]}}
        """);

    SeriesDetail series = adapter.findSeriesDetail(1396L);

    assertThat(series.cast())
        .containsExactly(new CastMember("A", null, null), new CastMember("B", null, null));
    assertThat(series.crew()).containsExactly(new CrewMember("Dave Porter", List.of("Composer")));
  }
}
