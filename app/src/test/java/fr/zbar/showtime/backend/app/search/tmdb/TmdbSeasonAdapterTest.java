package fr.zbar.showtime.backend.app.search.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
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
}
