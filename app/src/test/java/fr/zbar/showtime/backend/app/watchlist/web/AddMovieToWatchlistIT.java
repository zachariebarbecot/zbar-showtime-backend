package fr.zbar.showtime.backend.app.watchlist.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.TitleCatalog;
import java.util.List;
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
class AddMovieToWatchlistIT extends AbstractIT {

  @MockitoBean private TitleCatalog titleCatalog;

  @Autowired private JdbcTemplate jdbcTemplate;

  private String newUserEmail() {
    String email = "watchlist-" + UUID.randomUUID() + "@mail.fr";
    registerUser(email);
    return email;
  }

  private RestTestClient.ResponseSpec addMovie(String email, long tmdbId) {
    return restTestClient
        .post()
        .uri("/watchlist/movies")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .body(Map.of("tmdbId", tmdbId))
        .exchange();
  }

  @Test
  void should_return_201_with_a_to_watch_entry_when_the_movie_is_known() {
    String email = newUserEmail();
    when(titleCatalog.findMovie(603L))
        .thenReturn(new CatalogItem(603L, MediaType.MOVIE, "Matrix", null, null));

    addMovie(email, 603L)
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.tmdbId")
        .isEqualTo(603)
        .jsonPath("$.status")
        .isEqualTo("TO_WATCH")
        .jsonPath("$.addedAt")
        .isEqualTo("2025-06-15T10:00:00Z")
        .jsonPath("$.watchedAt")
        .doesNotExist();
    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            "select m.status, m.added_at, m.watched_at from watchlist_movie m"
                + " join users u on u.id = m.user_id where u.email = ? and m.tmdb_id = 603",
            email);
    assertThat(row).containsEntry("status", "TO_WATCH").containsEntry("watched_at", null);
  }

  private List<UUID> entryIds(String email) {
    return jdbcTemplate.queryForList(
        "select m.id from watchlist_movie m join users u on u.id = m.user_id"
            + " where u.email = ? and m.tmdb_id = 603",
        UUID.class,
        email);
  }

  @Test
  void should_return_409_and_keep_the_existing_entry_when_the_movie_is_already_in_the_watchlist() {
    String email = newUserEmail();
    when(titleCatalog.findMovie(603L))
        .thenReturn(new CatalogItem(603L, MediaType.MOVIE, "Matrix", null, null));
    addMovie(email, 603L).expectStatus().isCreated();
    List<UUID> existing = entryIds(email);

    addMovie(email, 603L)
        .expectStatus()
        .isEqualTo(409)
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Title already in watchlist");

    assertThat(entryIds(email)).isEqualTo(existing).hasSize(1);
  }

  @Test
  void should_return_201_for_each_user_when_two_users_add_the_same_movie() {
    String first = newUserEmail();
    String second = newUserEmail();
    when(titleCatalog.findMovie(603L))
        .thenReturn(new CatalogItem(603L, MediaType.MOVIE, "Matrix", null, null));
    addMovie(first, 603L).expectStatus().isCreated();

    addMovie(second, 603L).expectStatus().isCreated();

    assertThat(entryIds(first)).hasSize(1);
    assertThat(entryIds(second)).hasSize(1);
  }
}
