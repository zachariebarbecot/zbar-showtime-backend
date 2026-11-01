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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc
class AddSeriesToWatchlistIT extends AbstractIT {

  private static final CatalogItem GOT = new CatalogItem(1399L, MediaType.TV, "GoT", null, null);

  @MockitoBean private TitleCatalog titleCatalog;

  @Autowired private JdbcTemplate jdbcTemplate;

  private String newUserEmail() {
    String email = "watchlist-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    return email;
  }

  private RestTestClient.ResponseSpec addSeries(String email, long tmdbId) {
    return restTestClient
        .post()
        .uri("/watchlist/series")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", tmdbId))
        .exchange();
  }

  @Test
  void should_return_201_with_a_to_watch_entry_when_the_series_is_known() {
    String email = newUserEmail();
    when(titleCatalog.findSeries(1399L)).thenReturn(GOT);

    addSeries(email, 1399L)
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.tmdbId")
        .isEqualTo(1399)
        .jsonPath("$.status")
        .isEqualTo("TO_WATCH")
        .jsonPath("$.addedAt")
        .isEqualTo(FixedClockConfiguration.NOW.toString())
        .jsonPath("$.watchedAt")
        .doesNotExist();
    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            "select s.status, s.watched_at from watchlist_series s"
                + " join users u on u.id = s.user_id where u.email = ? and s.tmdb_id = 1399",
            email);
    assertThat(row).containsEntry("status", "TO_WATCH").containsEntry("watched_at", null);
  }
}
