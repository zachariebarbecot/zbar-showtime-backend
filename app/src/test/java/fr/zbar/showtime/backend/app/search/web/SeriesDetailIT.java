package fr.zbar.showtime.backend.app.search.web;

import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class SeriesDetailIT extends AbstractIT {

  @MockitoBean private SeriesDetailCatalog seriesDetailCatalog;

  private RestTestClient.ResponseSpec detail(String email, String id) {
    return restTestClient
        .get()
        .uri("/series/{id}", id)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange();
  }

  @Test
  void should_return_200_with_the_series_detail_when_tmdb_knows_the_series() {
    registerUser("series-ca1@mail.fr");
    when(seriesDetailCatalog.findSeriesDetail(1396L))
        .thenReturn(
            new SeriesDetail(
                1396L,
                "Breaking Bad",
                "Breaking Bad VO",
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

    detail("series-ca1@mail.fr", "1396")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.id")
        .isEqualTo(1396)
        .jsonPath("$.name")
        .isEqualTo("Breaking Bad")
        .jsonPath("$.originalName")
        .isEqualTo("Breaking Bad VO")
        .jsonPath("$.posterUrl")
        .isEqualTo("https://image.tmdb.org/t/p/w342/b.jpg")
        .jsonPath("$.firstAirDate")
        .isEqualTo("2008-01-20")
        .jsonPath("$.lastAirDate")
        .isEqualTo("2013-09-29")
        .jsonPath("$.numberOfSeasons")
        .isEqualTo(5)
        .jsonPath("$.numberOfEpisodes")
        .isEqualTo(62)
        .jsonPath("$.genres[1]")
        .isEqualTo("Crime")
        .jsonPath("$.overview")
        .isEqualTo("Un professeur de chimie devient fabricant de drogue.")
        .jsonPath("$.status")
        .isEqualTo("ENDED");
  }

  @Test
  void should_return_401_when_there_is_no_token() {
    restTestClient.get().uri("/series/1396").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void should_return_200_with_null_fields_and_empty_lists_when_the_series_has_no_data() {
    registerUser("series-ca2@mail.fr");
    when(seriesDetailCatalog.findSeriesDetail(1396L))
        .thenReturn(
            new SeriesDetail(
                1396L,
                "Breaking Bad",
                "Breaking Bad",
                List.of(),
                null,
                null,
                null,
                null,
                null,
                0,
                0,
                List.of(),
                List.of(),
                List.of()));

    detail("series-ca2@mail.fr", "1396")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.firstAirDate")
        .isEqualTo(null)
        .jsonPath("$.lastAirDate")
        .isEqualTo(null)
        .jsonPath("$.status")
        .isEqualTo(null)
        .jsonPath("$.overview")
        .isEqualTo(null)
        .jsonPath("$.genres")
        .isEmpty();
  }
}
