package fr.zbar.showtime.backend.app.watchlist.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.PageNumber;
import fr.zbar.showtime.backend.core.search.domain.SearchCatalog;
import fr.zbar.showtime.backend.core.search.domain.SearchPage;
import fr.zbar.showtime.backend.core.search.domain.SearchQuery;
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

  @Test
  void should_return_404_when_the_series_is_not_in_my_watchlist() {
    String email = newUser();

    remove(email, 1399L)
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.detail")
        .isEqualTo("Watchlist entry not found");
  }

  @Test
  void should_return_404_and_keep_the_series_of_another_user_when_only_they_have_it() {
    String owner = newUser();
    String other = newUser();
    addSeries(owner, 1399L);

    remove(other, 1399L).expectStatus().isNotFound();

    assertThat(seriesCount(owner, 1399L)).isEqualTo(1);
  }

  @Test
  void should_keep_the_series_of_the_other_user_when_both_have_it() {
    String first = newUser();
    String second = newUser();
    addSeries(first, 1399L);
    addSeries(second, 1399L);

    remove(first, 1399L).expectStatus().isNoContent();

    assertThat(seriesCount(first, 1399L)).isZero();
    assertThat(seriesCount(second, 1399L)).isEqualTo(1);
  }

  @Test
  void should_keep_the_movie_with_the_same_tmdb_id_when_the_series_is_removed() {
    String email = newUser();
    addMovie(email, 1399L);
    addSeries(email, 1399L);

    remove(email, 1399L).expectStatus().isNoContent();

    assertThat(seriesCount(email, 1399L)).isZero();
    assertThat(movieCount(email, 1399L)).isEqualTo(1);
  }

  @Test
  void should_expose_no_watchlist_status_and_allow_adding_again_when_the_series_was_removed() {
    String email = newUser();
    addSeries(email, 1399L);
    remove(email, 1399L).expectStatus().isNoContent();
    when(searchCatalog.searchSeries(new SearchQuery("thrones"), new PageNumber(1)))
        .thenReturn(
            new SearchPage(
                1,
                1,
                1,
                List.of(new CatalogItem(1399L, MediaType.TV, "Game of Thrones", null, null))));

    restTestClient
        .get()
        .uri("/search?query=thrones&type=TV")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginToken(email))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.results[0].watchlistStatus")
        .value(value -> assertThat(value).isNull());
    addSeries(email, 1399L);

    assertThat(
            jdbcTemplate.queryForObject(
                "select s.status from watchlist_series s"
                    + " join users u on u.id = s.user_id where u.email = ? and s.tmdb_id = 1399",
                String.class,
                email))
        .isEqualTo("TO_WATCH");
  }
}
