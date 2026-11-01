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
class ChangeMovieStatusIT extends AbstractIT {

  private static final CatalogItem MATRIX =
      new CatalogItem(603L, MediaType.MOVIE, "Matrix", null, null);

  @MockitoBean private TitleCatalog titleCatalog;

  @Autowired private JdbcTemplate jdbcTemplate;

  private String newUserWithMatrix() {
    String email = "watchlist-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    when(titleCatalog.findMovie(603L)).thenReturn(MATRIX);
    restTestClient
        .post()
        .uri("/watchlist/movies")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", 603L))
        .exchange()
        .expectStatus()
        .isCreated();
    return email;
  }

  private RestTestClient.ResponseSpec changeStatus(String email, long tmdbId, String status) {
    return restTestClient
        .patch()
        .uri("/watchlist/movies/{tmdbId}", tmdbId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("status", status))
        .exchange();
  }

  private Map<String, Object> row(String email) {
    return jdbcTemplate.queryForMap(
        "select m.status, m.watched_at from watchlist_movie m"
            + " join users u on u.id = m.user_id where u.email = ? and m.tmdb_id = 603",
        email);
  }

  @Test
  void should_return_200_with_a_watched_entry_when_the_movie_is_marked_as_watched() {
    String email = newUserWithMatrix();

    changeStatus(email, 603L, "WATCHED")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.tmdbId")
        .isEqualTo(603)
        .jsonPath("$.status")
        .isEqualTo("WATCHED")
        .jsonPath("$.watchedAt")
        .isEqualTo(FixedClockConfiguration.NOW.toString());
    assertThat(row(email))
        .containsEntry("status", "WATCHED")
        .extractingByKey("watched_at")
        .isNotNull();
  }

  @Test
  void should_return_200_without_watched_date_when_a_watched_movie_goes_back_to_to_watch() {
    String email = newUserWithMatrix();
    changeStatus(email, 603L, "WATCHED").expectStatus().isOk();

    changeStatus(email, 603L, "TO_WATCH")
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo("TO_WATCH")
        .jsonPath("$.watchedAt")
        .doesNotExist();
    assertThat(row(email)).containsEntry("status", "TO_WATCH").containsEntry("watched_at", null);
  }
}
