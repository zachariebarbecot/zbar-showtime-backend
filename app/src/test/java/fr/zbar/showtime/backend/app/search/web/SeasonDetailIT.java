package fr.zbar.showtime.backend.app.search.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.app.support.FixedClockConfiguration;
import fr.zbar.showtime.backend.app.support.SeriesDetails;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class SeasonDetailIT extends AbstractIT {

  @MockitoBean private SeasonCatalog seasonCatalog;

  @MockitoBean private TitleCatalog titleCatalog;

  @MockitoBean private SeriesDetailCatalog seriesDetailCatalog;

  private RestTestClient.ResponseSpec season(String email, String id, String seasonNumber) {
    return restTestClient
        .get()
        .uri("/series/{id}/seasons/{n}", id, seasonNumber)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange();
  }

  private String userWatching(int season, int... episodes) {
    String email = "season-watch-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    addToWatchlist(email);
    for (int episode : episodes) {
      markWatched(email, season, episode);
    }
    return email;
  }

  private void addToWatchlist(String email) {
    when(seriesDetailCatalog.findSeriesDetail(1396L)).thenReturn(SeriesDetails.returning(1396L));
    when(titleCatalog.findSeries(1396L))
        .thenReturn(new CatalogItem(1396L, MediaType.TV, "Breaking Bad", null, null));
    restTestClient
        .post()
        .uri("/watchlist/series")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", 1396L))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  private void markWatched(String email, int season, int episode) {
    restTestClient
        .post()
        .uri("/watchlist/series/1396/seasons/{s}/episodes/{e}/watched", season, episode)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  private void stubSeason(int number) {
    when(seasonCatalog.findSeason(1396L, number))
        .thenReturn(
            new SeasonDetail(
                number,
                "Saison " + number,
                null,
                null,
                null,
                List.of(
                    new Episode(1, "Pilote", null, null, null, null),
                    new Episode(2, "Le chat", null, null, null, null))));
  }

  @Test
  void should_expose_the_viewing_date_of_a_watched_episode_and_flag_the_others_unwatched() {
    stubSeason(1);
    String email = userWatching(1, 2);

    season(email, "1396", "1")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.episodes[1].watched")
        .isEqualTo(true)
        .jsonPath("$.episodes[1].watchedAt")
        .isEqualTo(FixedClockConfiguration.NOW.toString())
        .jsonPath("$.episodes[0].watched")
        .isEqualTo(false)
        .jsonPath("$.episodes[0].watchedAt")
        .doesNotExist();
  }

  @Test
  void should_flag_every_episode_unwatched_when_the_series_is_not_in_my_watchlist() {
    stubSeason(1);
    String email = "season-nowl-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);

    season(email, "1396", "1")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.episodes[0].watched")
        .isEqualTo(false)
        .jsonPath("$.episodes[0].watchedAt")
        .doesNotExist()
        .jsonPath("$.episodes[1].watched")
        .isEqualTo(false)
        .jsonPath("$.episodes[1].watchedAt")
        .doesNotExist();
  }

  @Test
  void should_flag_the_episode_unwatched_when_only_another_user_watched_it() {
    stubSeason(1);
    userWatching(1, 1);
    String me = userWatching(1);

    season(me, "1396", "1")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.episodes[0].watched")
        .isEqualTo(false)
        .jsonPath("$.episodes[0].watchedAt")
        .doesNotExist();
  }

  @Test
  void should_return_200_with_the_episodes_when_tmdb_knows_the_season() {
    registerUser("season-ca1@mail.fr");
    when(seasonCatalog.findSeason(1396L, 1))
        .thenReturn(
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
                    new Episode(2, "Le chat", null, null, null, null))));

    season("season-ca1@mail.fr", "1396", "1")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.seasonNumber")
        .isEqualTo(1)
        .jsonPath("$.name")
        .isEqualTo("Saison 1")
        .jsonPath("$.overview")
        .isEqualTo("Les débuts.")
        .jsonPath("$.airDate")
        .isEqualTo("2008-01-20")
        .jsonPath("$.posterUrl")
        .isEqualTo("https://image.tmdb.org/t/p/w342/s1.jpg")
        .jsonPath("$.episodes[0].episodeNumber")
        .isEqualTo(1)
        .jsonPath("$.episodes[0].name")
        .isEqualTo("Pilote")
        .jsonPath("$.episodes[0].overview")
        .isEqualTo("Walter apprend sa maladie.")
        .jsonPath("$.episodes[0].runtime")
        .isEqualTo(58)
        .jsonPath("$.episodes[0].airDate")
        .isEqualTo("2008-01-20")
        .jsonPath("$.episodes[0].stillUrl")
        .isEqualTo("https://image.tmdb.org/t/p/w300/e1.jpg")
        .jsonPath("$.episodes[1].episodeNumber")
        .isEqualTo(2);
  }

  @Test
  void should_return_401_when_there_is_no_token() {
    restTestClient.get().uri("/series/1396/seasons/1").exchange().expectStatus().isUnauthorized();
  }

  @Test
  void should_return_200_with_the_specials_when_the_season_number_is_zero() {
    registerUser("season-ca3@mail.fr");
    when(seasonCatalog.findSeason(1396L, 0))
        .thenReturn(
            new SeasonDetail(
                0,
                "Épisodes spéciaux",
                null,
                null,
                null,
                List.of(new Episode(1, "À venir", null, null, LocalDate.of(2999, 12, 31), null))));

    season("season-ca3@mail.fr", "1396", "0")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo("Épisodes spéciaux")
        .jsonPath("$.episodes[0].airDate")
        .isEqualTo("2999-12-31");
  }

  @ParameterizedTest
  @CsvSource({
    "0,1,tmdbId",
    "-1,1,tmdbId",
    "abc,1,tmdbId",
    "1396,-1,seasonNumber",
    "1396,abc,seasonNumber"
  })
  void should_return_400_without_calling_tmdb_when_an_id_is_invalid(
      String id, String seasonNumber, String field) {
    String email = "season-ca4a-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);

    season(email, id, seasonNumber)
        .expectStatus()
        .isBadRequest()
        .expectHeader()
        .contentTypeCompatibleWith("application/problem+json")
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo(field);

    verifyNoInteractions(seasonCatalog);
  }

  @Test
  void should_return_404_when_tmdb_does_not_know_the_series_or_the_season() {
    registerUser("season-ca4b@mail.fr");
    when(seasonCatalog.findSeason(999L, 1)).thenThrow(new TitleNotFoundException());

    season("season-ca4b@mail.fr", "999", "1")
        .expectStatus()
        .isNotFound()
        .expectHeader()
        .contentTypeCompatibleWith("application/problem+json");
  }

  @Test
  void should_return_502_without_technical_detail_when_the_catalog_is_unavailable() {
    registerUser("season-ca4c@mail.fr");
    when(seasonCatalog.findSeason(1396L, 1))
        .thenThrow(
            new CatalogUnavailableException(
                new IllegalStateException(
                    "401 from https://api.themoviedb.org/3 test-tmdb-token")));

    season("season-ca4c@mail.fr", "1396", "1")
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
