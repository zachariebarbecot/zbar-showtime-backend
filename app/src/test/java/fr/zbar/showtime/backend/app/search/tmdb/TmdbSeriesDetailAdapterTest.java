package fr.zbar.showtime.backend.app.search.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
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
}
