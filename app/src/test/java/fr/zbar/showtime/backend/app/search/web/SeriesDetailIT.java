package fr.zbar.showtime.backend.app.search.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.search.domain.CastMember;
import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.CrewMember;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeriesStatus;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

  @Test
  void should_return_the_creators_and_the_key_crew_when_the_series_has_a_crew() {
    registerUser("series-ca3@mail.fr");
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
                null,
                null,
                List.of("Vince Gilligan"),
                List.of(new CrewMember("Vince Gilligan", List.of("Writer", "Producer"))),
                List.of()));

    detail("series-ca3@mail.fr", "1396")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.creators[0]")
        .isEqualTo("Vince Gilligan")
        .jsonPath("$.crew[0].name")
        .isEqualTo("Vince Gilligan")
        .jsonPath("$.crew[0].jobs[1]")
        .isEqualTo("Producer");
  }

  @Test
  void should_return_the_cast_and_no_duration_when_the_series_has_actors() {
    registerUser("series-ca4@mail.fr");
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
                null,
                null,
                List.of(),
                List.of(),
                List.of(
                    new CastMember(
                        "Bryan Cranston",
                        "Walter White",
                        "https://image.tmdb.org/t/p/w185/b.jpg"))));

    detail("series-ca4@mail.fr", "1396")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.cast[0].name")
        .isEqualTo("Bryan Cranston")
        .jsonPath("$.cast[0].character")
        .isEqualTo("Walter White")
        .jsonPath("$.cast[0].photoUrl")
        .isEqualTo("https://image.tmdb.org/t/p/w185/b.jpg")
        .jsonPath("$.runtime")
        .doesNotExist();
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "-1", "abc"})
  void should_return_400_without_calling_tmdb_when_the_id_is_invalid(String id) {
    String email = "series-ca5a-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);

    detail(email, id)
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentTypeCompatibleWith("application/problem+json")
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("tmdbId");

    verifyNoInteractions(seriesDetailCatalog);
  }

  @Test
  void should_return_404_when_tmdb_does_not_know_the_series() {
    registerUser("series-ca5b@mail.fr");
    when(seriesDetailCatalog.findSeriesDetail(999L)).thenThrow(new TitleNotFoundException());

    detail("series-ca5b@mail.fr", "999")
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentTypeCompatibleWith("application/problem+json");
  }

  @Test
  void should_return_502_without_technical_detail_when_the_catalog_is_unavailable() {
    registerUser("series-ca5c@mail.fr");
    when(seriesDetailCatalog.findSeriesDetail(1396L))
        .thenThrow(
            new CatalogUnavailableException(
                new IllegalStateException(
                    "401 from https://api.themoviedb.org/3 test-tmdb-token")));

    detail("series-ca5c@mail.fr", "1396")
        .expectStatus()
        .isEqualTo(502)
        .expectHeader()
        .contentTypeCompatibleWith("application/problem+json")
        .expectBody(String.class)
        .value(
            body ->
                assertThat(body)
                    .contains("\"status\":502")
                    .doesNotContain("test-tmdb-token", "themoviedb", "Exception"));
  }
}
