package fr.zbar.showtime.backend.app.watchlist.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.app.support.FixedClockConfiguration;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class ChangeSeriesStatusIT extends AbstractIT {

  private static final CatalogItem GOT = new CatalogItem(1399L, MediaType.TV, "GoT", null, null);

  @MockitoBean private TitleCatalog titleCatalog;

  @Autowired private JdbcTemplate jdbcTemplate;

  private String newUserWithGot() {
    String email = "watchlist-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    when(titleCatalog.findSeries(1399L)).thenReturn(GOT);
    restTestClient
        .post()
        .uri("/watchlist/series")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", 1399L))
        .exchange()
        .expectStatus()
        .isCreated();
    return email;
  }

  private RestTestClient.ResponseSpec changeStatus(String email, long tmdbId, String status) {
    return restTestClient
        .patch()
        .uri("/watchlist/series/{tmdbId}", tmdbId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("status", status))
        .exchange();
  }

  private Map<String, Object> row(String email) {
    return jdbcTemplate.queryForMap(
        "select s.status, s.watched_at from watchlist_series s"
            + " join users u on u.id = s.user_id where u.email = ? and s.tmdb_id = 1399",
        email);
  }

  @Test
  void should_return_200_with_a_watched_entry_when_the_series_is_marked_as_watched() {
    String email = newUserWithGot();

    changeStatus(email, 1399L, "WATCHED")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.tmdbId")
        .isEqualTo(1399)
        .jsonPath("$.status")
        .isEqualTo("WATCHED")
        .jsonPath("$.watchedAt")
        .isEqualTo(FixedClockConfiguration.NOW.toString());
    assertThat(row(email))
        .containsEntry("status", "WATCHED")
        .extractingByKey("watched_at")
        .isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"TO_WATCH", "WATCHING", "DROPPED"})
  void should_return_200_without_watched_date_when_a_watched_series_changes_status(String status) {
    String email = newUserWithGot();
    changeStatus(email, 1399L, "WATCHED").expectStatus().isOk();

    changeStatus(email, 1399L, status)
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(status)
        .jsonPath("$.watchedAt")
        .doesNotExist();
    assertThat(row(email)).containsEntry("status", status).containsEntry("watched_at", null);
  }

  @ParameterizedTest
  @ValueSource(strings = {"WATCHING", "DROPPED"})
  void should_return_200_without_watched_date_when_a_to_watch_series_changes_status(String status) {
    String email = newUserWithGot();

    changeStatus(email, 1399L, status)
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(status)
        .jsonPath("$.watchedAt")
        .doesNotExist();
    assertThat(row(email)).containsEntry("status", status).containsEntry("watched_at", null);
  }

  @ParameterizedTest
  @ValueSource(strings = {"unknown", "watched"})
  void should_return_400_and_keep_the_entry_when_the_status_is_not_allowed(String status) {
    String email = newUserWithGot();

    changeStatus(email, 1399L, status)
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.errors[0].field")
        .isEqualTo("status")
        .jsonPath("$.errors[0].message")
        .isEqualTo("Status must be TO_WATCH, WATCHING, WATCHED or DROPPED");
    assertThat(row(email)).containsEntry("status", "TO_WATCH").containsEntry("watched_at", null);
  }
}
