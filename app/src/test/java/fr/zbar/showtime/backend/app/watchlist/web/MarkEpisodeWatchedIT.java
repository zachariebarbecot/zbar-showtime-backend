package fr.zbar.showtime.backend.app.watchlist.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.app.support.FixedClockConfiguration;
import fr.zbar.showtime.backend.app.support.SeriesDetails;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.Episode;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.SeasonCatalog;
import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import fr.zbar.showtime.backend.core.search.domain.SeasonSummary;
import fr.zbar.showtime.backend.core.search.domain.SeriesDetailCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class MarkEpisodeWatchedIT extends AbstractIT {

  private static final long SERIES_ID = 1396L;

  @MockitoBean private TitleCatalog titleCatalog;

  @MockitoBean private SeasonCatalog seasonCatalog;

  @MockitoBean private SeriesDetailCatalog seriesDetailCatalog;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void stubCatalogs() {
    when(seriesDetailCatalog.findSeriesDetail(SERIES_ID))
        .thenReturn(SeriesDetails.returning(SERIES_ID));
    when(titleCatalog.findSeries(SERIES_ID))
        .thenReturn(new CatalogItem(SERIES_ID, MediaType.TV, "Breaking Bad", null, null));
    when(seasonCatalog.findSeason(SERIES_ID, 1))
        .thenReturn(
            new SeasonDetail(
                1,
                "Saison 1",
                null,
                null,
                null,
                List.of(
                    new Episode(1, "Pilote", null, null, null, null),
                    new Episode(2, "Le chat", null, null, null, null))));
    when(seasonCatalog.findSeason(SERIES_ID, 9)).thenThrow(new TitleNotFoundException());
  }

  private String newUserWithSeries() {
    String email = newUser();
    restTestClient
        .post()
        .uri("/watchlist/series")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", SERIES_ID))
        .exchange()
        .expectStatus()
        .isCreated();
    return email;
  }

  private String newUser() {
    String email = "watched-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    return email;
  }

  private RestTestClient.ResponseSpec mark(String email, int season, int episode) {
    return restTestClient
        .post()
        .uri(
            "/watchlist/series/{tmdbId}/seasons/{season}/episodes/{episode}/watched",
            SERIES_ID,
            season,
            episode)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange();
  }

  private List<Map<String, Object>> viewings(String email) {
    return jdbcTemplate.queryForList(
        "select w.season_number, w.episode_number, w.watched_at from watched_episode w"
            + " join users u on u.id = w.user_id where u.email = ? and w.series_tmdb_id = ?",
        email,
        SERIES_ID);
  }

  @Test
  void should_return_201_without_body_and_save_the_viewing_with_the_server_date() {
    String email = newUserWithSeries();

    mark(email, 1, 2).expectStatus().isCreated().expectBody().isEmpty();

    assertThat(viewings(email)).hasSize(1);
    Map<String, Object> viewing = viewings(email).getFirst();
    assertThat(viewing).containsEntry("season_number", 1).containsEntry("episode_number", 2);
    assertThat(((Timestamp) viewing.get("watched_at")).toInstant())
        .isEqualTo(FixedClockConfiguration.NOW);
  }

  private String seriesStatus(String email) {
    return jdbcTemplate.queryForObject(
        "select s.status from watchlist_series s join users u on u.id = s.user_id"
            + " where u.email = ? and s.tmdb_id = ?",
        String.class,
        email,
        SERIES_ID);
  }

  @Test
  void should_set_the_series_to_watching_when_an_episode_is_marked() {
    String email = newUserWithSeries();

    mark(email, 1, 1).expectStatus().isCreated();

    assertThat(seriesStatus(email)).isEqualTo("WATCHING");
  }

  @Test
  void should_return_201_and_keep_a_single_viewing_when_the_episode_is_marked_again() {
    String email = newUserWithSeries();
    mark(email, 1, 2).expectStatus().isCreated();

    mark(email, 1, 2).expectStatus().isCreated().expectBody().isEmpty();

    assertThat(viewings(email)).hasSize(1);
  }

  @Test
  void should_return_404_and_save_nothing_when_the_series_is_not_in_my_watchlist() {
    String email = newUser();

    mark(email, 1, 1)
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Watchlist entry not found");

    assertThat(viewings(email)).isEmpty();
  }

  @Test
  void should_return_404_and_save_nothing_when_the_season_is_unknown() {
    String email = newUserWithSeries();

    mark(email, 9, 1).expectStatus().isNotFound();

    assertThat(viewings(email)).isEmpty();
  }

  @Test
  void should_return_404_and_save_nothing_when_the_episode_is_unknown() {
    String email = newUserWithSeries();

    mark(email, 1, 9).expectStatus().isNotFound();

    assertThat(viewings(email)).isEmpty();
  }

  @Test
  void should_not_mark_the_episode_for_the_other_user_when_one_user_marks_it() {
    String first = newUserWithSeries();
    String second = newUserWithSeries();

    mark(first, 1, 1).expectStatus().isCreated();

    assertThat(viewings(second)).isEmpty();
    mark(second, 1, 1).expectStatus().isCreated();
    assertThat(viewings(first)).hasSize(1);
    assertThat(viewings(second)).hasSize(1);
  }

  @Test
  void should_set_the_series_to_watched_when_a_finished_series_has_all_regular_episodes_watched() {
    when(seriesDetailCatalog.findSeriesDetail(SERIES_ID))
        .thenReturn(
            SeriesDetails.ended(
                SERIES_ID,
                new SeasonSummary(0, "Spéciaux", 3, null, null),
                new SeasonSummary(1, "Saison 1", 2, null, null)));
    when(seasonCatalog.findSeason(SERIES_ID, 0))
        .thenReturn(
            new SeasonDetail(
                0,
                "Spéciaux",
                null,
                null,
                null,
                List.of(new Episode(1, "Bonus", null, null, null, null))));
    String email = newUserWithSeries();
    mark(email, 0, 1).expectStatus().isCreated();
    mark(email, 1, 1).expectStatus().isCreated();

    mark(email, 1, 2).expectStatus().isCreated();

    assertThat(seriesStatus(email)).isEqualTo("WATCHED");
    assertThat(seriesWatchedAt(email)).isEqualTo(FixedClockConfiguration.NOW);
  }

  private java.time.Instant seriesWatchedAt(String email) {
    return jdbcTemplate
        .queryForObject(
            "select s.watched_at from watchlist_series s join users u on u.id = s.user_id"
                + " where u.email = ? and s.tmdb_id = ?",
            Timestamp.class,
            email,
            SERIES_ID)
        .toInstant();
  }
}
