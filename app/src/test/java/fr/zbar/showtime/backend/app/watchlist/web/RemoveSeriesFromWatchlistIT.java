package fr.zbar.showtime.backend.app.watchlist.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.SearchCatalog;
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
class RemoveSeriesFromWatchlistIT extends AbstractIT {

  @MockitoBean private TitleCatalog titleCatalog;

  @MockitoBean private SearchCatalog searchCatalog;

  @Autowired private JdbcTemplate jdbcTemplate;

  private String newUser() {
    String email = "watchlist-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    return email;
  }

  private void addMovie(String email, long tmdbId) {
    when(titleCatalog.findMovie(tmdbId))
        .thenReturn(new CatalogItem(tmdbId, MediaType.MOVIE, "Fight Club", null, null));
    restTestClient
        .post()
        .uri("/watchlist/movies")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", tmdbId))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  private void addSeries(String email, long tmdbId) {
    when(titleCatalog.findSeries(tmdbId))
        .thenReturn(new CatalogItem(tmdbId, MediaType.TV, "Game of Thrones", null, null));
    restTestClient
        .post()
        .uri("/watchlist/series")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", tmdbId))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  private RestTestClient.ResponseSpec remove(String email, long tmdbId) {
    return restTestClient
        .delete()
        .uri("/watchlist/series/{tmdbId}", tmdbId)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange();
  }

  private int seriesCount(String email, long tmdbId) {
    return jdbcTemplate.queryForObject(
        "select count(*) from watchlist_series s"
            + " join users u on u.id = s.user_id where u.email = ? and s.tmdb_id = ?",
        Integer.class,
        email,
        tmdbId);
  }

  private int movieCount(String email, long tmdbId) {
    return jdbcTemplate.queryForObject(
        "select count(*) from watchlist_movie m"
            + " join users u on u.id = m.user_id where u.email = ? and m.tmdb_id = ?",
        Integer.class,
        email,
        tmdbId);
  }

  @Test
  void should_return_204_without_body_and_remove_the_series_when_it_is_in_my_watchlist() {
    String email = newUser();
    addSeries(email, 1399L);

    remove(email, 1399L).expectStatus().isNoContent().expectBody().isEmpty();

    assertThat(seriesCount(email, 1399L)).isZero();
  }
}
