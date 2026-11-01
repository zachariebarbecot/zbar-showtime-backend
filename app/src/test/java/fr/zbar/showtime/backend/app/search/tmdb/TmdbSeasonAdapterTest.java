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

import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TmdbSeasonAdapterTest {

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
  private final TmdbSeasonAdapter adapter =
      new TmdbSeasonAdapter(
          TmdbConfiguration.seriesApi(TmdbConfiguration.authenticated(builder, PROPERTIES)),
          PROPERTIES);

  private void respondWith(int seasonNumber, String json) {
    server
        .expect(
            requestTo(
                Matchers.startsWith(
                    "https://api.tmdb.test/3/tv/1396/season/" + seasonNumber + "?")))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer secret-token"))
        .andExpect(queryParam("language", "fr-FR"))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
  }

  @Test
  void should_map_the_season_and_its_episodes_in_tmdb_order_when_tmdb_knows_the_season() {
    respondWith(
        1,
        """
        {"season_number":1,"name":"Saison 1","overview":"Les débuts.","air_date":"2008-01-20",
         "poster_path":"/s1.jpg","episodes":[
          {"episode_number":1,"name":"Pilote","overview":"Walter apprend sa maladie.",
           "runtime":58,"air_date":"2008-01-20","still_path":"/e1.jpg"},
          {"episode_number":2,"name":"Le chat","overview":"Nettoyage.",
           "runtime":48,"air_date":"2008-01-27","still_path":"/e2.jpg"}]}
        """);

    SeasonDetail season = adapter.findSeason(1396L, 1);

    server.verify();
    assertThat(season)
        .isEqualTo(
            new SeasonDetail(
                1,
                "Saison 1",
                "Les débuts.",
                LocalDate.of(2008, 1, 20),
                "https://image.tmdb.org/t/p/w342/s1.jpg",
                List.of(
                    new Episode(
                        1,
                        "Pilote",
                        "Walter apprend sa maladie.",
                        58,
                        LocalDate.of(2008, 1, 20),
                        "https://image.tmdb.org/t/p/w300/e1.jpg"),
                    new Episode(
                        2,
                        "Le chat",
                        "Nettoyage.",
                        48,
                        LocalDate.of(2008, 1, 27),
                        "https://image.tmdb.org/t/p/w300/e2.jpg"))));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        """
        {"season_number":1,"episodes":[{"episode_number":3,"name":"Sans rien","overview":"",
         "runtime":0,"air_date":"","still_path":""}]}
        """,
        """
        {"season_number":1,"episodes":[{"episode_number":3,"name":"Sans rien","overview":null,
         "runtime":null,"air_date":null,"still_path":null}]}
        """,
        """
        {"season_number":1,"episodes":[{"episode_number":3,"name":"Sans rien"}]}
        """
      })
  void should_keep_the_episode_with_null_fields_when_tmdb_has_no_data_for_it(String json) {
    respondWith(1, json);

    assertThat(adapter.findSeason(1396L, 1).episodes())
        .containsExactly(new Episode(3, "Sans rien", null, null, null, null));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        """
        {"season_number":1,"name":"Saison 1","overview":"","air_date":"","poster_path":""}
        """,
        """
        {"season_number":1,"name":"Saison 1","overview":null,"air_date":null,
         "poster_path":null,"episodes":null}
        """
      })
  void should_return_a_season_with_null_fields_and_no_episodes_when_tmdb_has_no_data(String json) {
    respondWith(1, json);

    assertThat(adapter.findSeason(1396L, 1))
        .isEqualTo(new SeasonDetail(1, "Saison 1", null, null, null, List.of()));
  }

  @Test
  void should_keep_the_episodes_with_their_future_air_date_when_the_season_is_the_specials() {
    respondWith(
        0,
        """
        {"season_number":0,"name":"Épisodes spéciaux","episodes":[
          {"episode_number":1,"name":"À venir","air_date":"2999-12-31"}]}
        """);

    assertThat(adapter.findSeason(1396L, 0))
        .isEqualTo(
            new SeasonDetail(
                0,
                "Épisodes spéciaux",
                null,
                null,
                null,
                List.of(new Episode(1, "À venir", null, null, LocalDate.of(2999, 12, 31), null))));
  }

  private void respondWithStatus(HttpStatus status) {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/tv/1396/season/1?")))
        .andRespond(withStatus(status));
  }

  @Test
  void should_throw_title_not_found_when_tmdb_answers_404() {
    respondWithStatus(HttpStatus.NOT_FOUND);

    assertThatThrownBy(() -> adapter.findSeason(1396L, 1))
        .isInstanceOf(TitleNotFoundException.class);
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpStatus.class,
      names = {"INTERNAL_SERVER_ERROR", "SERVICE_UNAVAILABLE", "TOO_MANY_REQUESTS", "UNAUTHORIZED"})
  void should_throw_catalog_unavailable_when_tmdb_answers_an_error(HttpStatus status) {
    respondWithStatus(status);

    assertThatThrownBy(() -> adapter.findSeason(1396L, 1))
        .isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_throw_catalog_unavailable_when_tmdb_times_out() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/tv/1396/season/1?")))
        .andRespond(withException(new SocketTimeoutException("read timed out")));

    assertThatThrownBy(() -> adapter.findSeason(1396L, 1))
        .isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_throw_catalog_unavailable_when_tmdb_answers_an_empty_body() {
    respondWith(1, "");

    assertThatThrownBy(() -> adapter.findSeason(1396L, 1))
        .isInstanceOf(CatalogUnavailableException.class);
  }
}
