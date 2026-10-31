package fr.zbar.showtime.backend.app.search.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;
import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.MediaType;
import fr.zbar.showtime.backend.core.search.domain.PageNumber;
import fr.zbar.showtime.backend.core.search.domain.SearchPage;
import fr.zbar.showtime.backend.core.search.domain.SearchQuery;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TmdbSearchAdapterTest {

  private static final TmdbProperties PROPERTIES =
      new TmdbProperties(
          "https://api.tmdb.test/3",
          "secret-token",
          "https://image.tmdb.org/t/p",
          "w342",
          "fr-FR",
          Duration.ofSeconds(1),
          Duration.ofSeconds(2));

  private final RestClient.Builder builder = RestClient.builder();
  private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
  private final TmdbSearchAdapter adapter =
      new TmdbSearchAdapter(TmdbConfiguration.authenticated(builder, PROPERTIES), PROPERTIES);

  @Test
  void should_call_search_movie_with_language_page_token_and_the_query_encoded_intact() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/movie?")))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer secret-token"))
        .andExpect(queryParam("query", "L%27%C3%A9t%C3%A9%20%26%20co"))
        .andExpect(queryParam("language", "fr-FR"))
        .andExpect(queryParam("include_adult", "false"))
        .andExpect(queryParam("page", "2"))
        .andRespond(
            withSuccess(
                """
                {"page":2,"total_pages":7,"total_results":123,"results":[
                  {"id":603,"title":"Matrix","poster_path":"/m.jpg","release_date":"1999-03-31"},
                  {"id":604,"title":"Matrix Reloaded","poster_path":"/r.jpg","release_date":"2003-05-15"}
                ]}
                """,
                org.springframework.http.MediaType.APPLICATION_JSON));

    SearchPage result = adapter.searchMovies(new SearchQuery("L'été & co"), new PageNumber(2));

    server.verify();
    assertThat(result.page()).isEqualTo(2);
    assertThat(result.totalPages()).isEqualTo(7);
    assertThat(result.totalResults()).isEqualTo(123);
    assertThat(result.results())
        .containsExactly(
            new CatalogItem(
                603L,
                MediaType.MOVIE,
                "Matrix",
                "https://image.tmdb.org/t/p/w342/m.jpg",
                LocalDate.of(1999, 3, 31)),
            new CatalogItem(
                604L,
                MediaType.MOVIE,
                "Matrix Reloaded",
                "https://image.tmdb.org/t/p/w342/r.jpg",
                LocalDate.of(2003, 5, 15)));
  }

  @Test
  void should_call_search_tv_and_map_name_and_first_air_date_to_title_and_release_date() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/tv?")))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer secret-token"))
        .andExpect(queryParam("query", "dark"))
        .andExpect(queryParam("language", "fr-FR"))
        .andExpect(queryParam("include_adult", "false"))
        .andExpect(queryParam("page", "2"))
        .andRespond(
            withSuccess(
                """
                {"page":2,"total_pages":3,"total_results":45,"results":[
                  {"id":70523,"name":"Dark","poster_path":"/d.jpg","first_air_date":"2017-12-01"}
                ]}
                """,
                org.springframework.http.MediaType.APPLICATION_JSON));

    SearchPage result = adapter.searchSeries(new SearchQuery("dark"), new PageNumber(2));

    server.verify();
    assertThat(result)
        .isEqualTo(
            new SearchPage(
                2,
                3,
                45,
                List.of(
                    new CatalogItem(
                        70523L,
                        MediaType.TV,
                        "Dark",
                        "https://image.tmdb.org/t/p/w342/d.jpg",
                        LocalDate.of(2017, 12, 1)))));
  }

  @Test
  void should_call_search_multi_and_keep_the_native_order_of_movies_and_series() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/multi?")))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Authorization", "Bearer secret-token"))
        .andExpect(queryParam("query", "star"))
        .andExpect(queryParam("language", "fr-FR"))
        .andExpect(queryParam("include_adult", "false"))
        .andExpect(queryParam("page", "2"))
        .andRespond(
            withSuccess(
                """
                {"page":2,"total_pages":5,"total_results":90,"results":[
                  {"id":1,"media_type":"tv","name":"Star Trek","poster_path":"/s.jpg","first_air_date":"1966-09-08"},
                  {"id":11,"media_type":"movie","title":"Star Wars","poster_path":"/w.jpg","release_date":"1977-05-25"}
                ]}
                """,
                org.springframework.http.MediaType.APPLICATION_JSON));

    SearchPage result = adapter.searchAll(new SearchQuery("star"), new PageNumber(2));

    server.verify();
    assertThat(result)
        .isEqualTo(
            new SearchPage(
                2,
                5,
                90,
                List.of(
                    new CatalogItem(
                        1L,
                        MediaType.TV,
                        "Star Trek",
                        "https://image.tmdb.org/t/p/w342/s.jpg",
                        LocalDate.of(1966, 9, 8)),
                    new CatalogItem(
                        11L,
                        MediaType.MOVIE,
                        "Star Wars",
                        "https://image.tmdb.org/t/p/w342/w.jpg",
                        LocalDate.of(1977, 5, 25)))));
  }

  private void respondWithMulti(String json) {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/multi?")))
        .andRespond(withSuccess(json, org.springframework.http.MediaType.APPLICATION_JSON));
  }

  private SearchPage searchAll() {
    return adapter.searchAll(new SearchQuery("star"), new PageNumber(1));
  }

  @Test
  void should_drop_people_from_multi_results_and_keep_the_tmdb_totals() {
    respondWithMulti(
        """
        {"page":1,"total_pages":4,"total_results":80,"results":[
          {"id":9,"media_type":"person","name":"Star Person","profile_path":"/p.jpg"},
          {"id":11,"media_type":"movie","title":"Star Wars","release_date":"1977-05-25"},
          {"id":12,"media_type":"person","name":"Other"}
        ]}
        """);

    SearchPage result = searchAll();

    assertThat(result.totalPages()).isEqualTo(4);
    assertThat(result.totalResults()).isEqualTo(80);
    assertThat(result.results())
        .containsExactly(
            new CatalogItem(11L, MediaType.MOVIE, "Star Wars", null, LocalDate.of(1977, 5, 25)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"\"poster_path\":null,", "\"poster_path\":\"\",", ""})
  void should_keep_multi_results_with_a_null_poster_url_when_the_poster_is_null_empty_or_absent(
      String posterField) {
    respondWithMulti(
        """
        {"page":1,"total_pages":1,"total_results":2,"results":[
          {"id":1,"media_type":"movie",%s"title":"Star Wars","release_date":"1977-05-25"},
          {"id":2,"media_type":"tv",%s"name":"Star Trek","first_air_date":"1966-09-08"}]}
        """
            .formatted(posterField, posterField));

    assertThat(searchAll().results())
        .containsExactly(
            new CatalogItem(1L, MediaType.MOVIE, "Star Wars", null, LocalDate.of(1977, 5, 25)),
            new CatalogItem(2L, MediaType.TV, "Star Trek", null, LocalDate.of(1966, 9, 8)));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "\"release_date\":null,\"first_air_date\":null,",
        "\"release_date\":\"\",\"first_air_date\":\"\",",
        ""
      })
  void should_keep_multi_results_with_a_null_release_date_when_the_date_is_null_empty_or_absent(
      String dateField) {
    respondWithMulti(
        """
        {"page":1,"total_pages":1,"total_results":2,"results":[
          {"id":1,"media_type":"movie",%s"title":"Star Wars"},
          {"id":2,"media_type":"tv",%s"name":"Star Trek"}]}
        """
            .formatted(dateField, dateField));

    assertThat(searchAll().results())
        .containsExactly(
            new CatalogItem(1L, MediaType.MOVIE, "Star Wars", null, null),
            new CatalogItem(2L, MediaType.TV, "Star Trek", null, null));
  }

  @Test
  void should_map_each_multi_result_with_the_fields_of_its_own_media_type() {
    respondWithMulti(
        """
        {"page":1,"total_pages":1,"total_results":2,"results":[
          {"id":1,"media_type":"movie","title":"Star Wars","name":"ignored","release_date":"1977-05-25","first_air_date":"2000-01-01"},
          {"id":2,"media_type":"tv","title":"ignored","name":"Star Trek","release_date":"2000-01-01","first_air_date":"1966-09-08"}]}
        """);

    assertThat(searchAll().results())
        .containsExactly(
            new CatalogItem(1L, MediaType.MOVIE, "Star Wars", null, LocalDate.of(1977, 5, 25)),
            new CatalogItem(2L, MediaType.TV, "Star Trek", null, LocalDate.of(1966, 9, 8)));
  }

  @ParameterizedTest
  @ValueSource(ints = {401, 429, 500, 503})
  void should_fail_with_catalog_unavailable_for_multi_when_tmdb_answers_an_error_status(
      int status) {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/multi?")))
        .andRespond(withStatus(HttpStatus.valueOf(status)));

    assertThatThrownBy(this::searchAll).isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_fail_with_catalog_unavailable_for_multi_when_tmdb_times_out() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/multi?")))
        .andRespond(withException(new SocketTimeoutException("Read timed out")));

    assertThatThrownBy(this::searchAll).isInstanceOf(CatalogUnavailableException.class);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "not json at all",
        "{}",
        "{\"page\":1,\"total_pages\":1,\"total_results\":1,\"results\":[{\"id\":1,\"title\":\"S\"}]}",
        "{\"page\":1,\"total_pages\":1,\"total_results\":1,\"results\":[{\"id\":1,\"media_type\":\"movie\",\"title\":\"S\",\"release_date\":\"25/05/1977\"}]}"
      })
  void should_fail_with_catalog_unavailable_for_multi_when_the_body_has_an_unexpected_format(
      String body) {
    respondWithMulti(body);

    assertThatThrownBy(this::searchAll).isInstanceOf(CatalogUnavailableException.class);
  }

  private void respondWith(String json) {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/movie?")))
        .andRespond(withSuccess(json, org.springframework.http.MediaType.APPLICATION_JSON));
  }

  private void respondWithSeries(String json) {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/tv?")))
        .andRespond(withSuccess(json, org.springframework.http.MediaType.APPLICATION_JSON));
  }

  private SearchPage search() {
    return adapter.searchMovies(new SearchQuery("matrix"), new PageNumber(1));
  }

  private SearchPage searchSeries() {
    return adapter.searchSeries(new SearchQuery("dark"), new PageNumber(1));
  }

  @ParameterizedTest
  @ValueSource(strings = {"\"poster_path\":null,", "\"poster_path\":\"\",", ""})
  void should_map_a_null_poster_url_and_keep_the_item_when_the_poster_is_null_empty_or_absent(
      String posterField) {
    respondWith(
        """
        {"page":1,"total_pages":1,"total_results":1,"results":[
          {"id":1,%s"title":"Matrix","release_date":"1999-03-31"}]}
        """
            .formatted(posterField));

    assertThat(search().results())
        .containsExactly(
            new CatalogItem(1L, MediaType.MOVIE, "Matrix", null, LocalDate.of(1999, 3, 31)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"\"release_date\":null,", "\"release_date\":\"\",", ""})
  void should_map_a_null_release_date_and_keep_the_item_when_the_date_is_null_empty_or_absent(
      String dateField) {
    respondWith(
        """
        {"page":1,"total_pages":1,"total_results":1,"results":[
          {"id":1,%s"title":"Matrix","poster_path":"/m.jpg"}]}
        """
            .formatted(dateField));

    assertThat(search().results())
        .containsExactly(
            new CatalogItem(
                1L, MediaType.MOVIE, "Matrix", "https://image.tmdb.org/t/p/w342/m.jpg", null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"\"poster_path\":null,", "\"poster_path\":\"\",", ""})
  void should_keep_the_series_with_a_null_poster_url_when_the_poster_is_null_empty_or_absent(
      String posterField) {
    respondWithSeries(
        """
        {"page":1,"total_pages":1,"total_results":1,"results":[
          {"id":1,%s"name":"Dark","first_air_date":"2017-12-01"}]}
        """
            .formatted(posterField));

    assertThat(searchSeries().results())
        .containsExactly(
            new CatalogItem(1L, MediaType.TV, "Dark", null, LocalDate.of(2017, 12, 1)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"\"first_air_date\":null,", "\"first_air_date\":\"\",", ""})
  void should_keep_the_series_with_a_null_release_date_when_the_date_is_null_empty_or_absent(
      String dateField) {
    respondWithSeries(
        """
        {"page":1,"total_pages":1,"total_results":1,"results":[
          {"id":1,%s"name":"Dark","poster_path":"/d.jpg"}]}
        """
            .formatted(dateField));

    assertThat(searchSeries().results())
        .containsExactly(
            new CatalogItem(
                1L, MediaType.TV, "Dark", "https://image.tmdb.org/t/p/w342/d.jpg", null));
  }

  @Test
  void should_return_an_empty_page_when_tmdb_finds_nothing() {
    respondWith("{\"page\":1,\"total_pages\":0,\"total_results\":0,\"results\":[]}");

    assertThat(search()).isEqualTo(new SearchPage(1, 0, 0, List.of()));
  }

  @Test
  void should_return_an_empty_page_keeping_the_totals_when_the_page_is_beyond_the_last_one() {
    respondWith("{\"page\":9,\"total_pages\":3,\"total_results\":50,\"results\":[]}");

    assertThat(adapter.searchMovies(new SearchQuery("matrix"), new PageNumber(9)))
        .isEqualTo(new SearchPage(9, 3, 50, List.of()));
  }

  @ParameterizedTest
  @ValueSource(ints = {401, 403, 404, 429, 500, 502, 503})
  void should_fail_with_catalog_unavailable_when_tmdb_answers_an_error_status(int status) {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/movie?")))
        .andRespond(withStatus(HttpStatus.valueOf(status)));

    assertThatThrownBy(this::search).isInstanceOf(CatalogUnavailableException.class);
  }

  @ParameterizedTest
  @ValueSource(ints = {401, 429, 500, 503})
  void should_fail_with_catalog_unavailable_for_series_when_tmdb_answers_an_error_status(
      int status) {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/tv?")))
        .andRespond(withStatus(HttpStatus.valueOf(status)));

    assertThatThrownBy(() -> searchSeries()).isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_fail_with_catalog_unavailable_for_series_when_tmdb_times_out() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/tv?")))
        .andRespond(withException(new SocketTimeoutException("Read timed out")));

    assertThatThrownBy(() -> searchSeries()).isInstanceOf(CatalogUnavailableException.class);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "not json at all",
        "{}",
        "{\"page\":1,\"total_pages\":1,\"total_results\":1,\"results\":[{\"id\":1,\"name\":\"D\",\"first_air_date\":\"01/12/2017\"}]}"
      })
  void should_fail_with_catalog_unavailable_for_series_when_the_body_has_an_unexpected_format(
      String body) {
    respondWithSeries(body);

    assertThatThrownBy(() -> searchSeries()).isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_fail_with_catalog_unavailable_when_tmdb_times_out() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/movie?")))
        .andRespond(withException(new SocketTimeoutException("Read timed out")));

    assertThatThrownBy(this::search).isInstanceOf(CatalogUnavailableException.class);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "not json at all",
        "[]",
        "{}",
        "{\"page\":1,\"total_pages\":1,\"total_results\":1,\"results\":\"oops\"}",
        "{\"page\":1,\"total_pages\":1,\"total_results\":1,\"results\":[{\"id\":1,\"title\":\"M\",\"release_date\":\"31/03/1999\"}]}"
      })
  void should_fail_with_catalog_unavailable_when_the_tmdb_body_has_an_unexpected_format(
      String body) {
    respondWith(body);

    assertThatThrownBy(this::search).isInstanceOf(CatalogUnavailableException.class);
  }

  @Test
  void should_not_leak_the_token_or_the_url_in_the_failure_message() {
    server
        .expect(requestTo(startsWith("https://api.tmdb.test/3/search/movie?")))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

    assertThatThrownBy(this::search)
        .isInstanceOfSatisfying(
            CatalogUnavailableException.class,
            exception -> {
              assertThat(exception.getMessage()).isNull();
              assertThat(exception.toString()).doesNotContain("secret-token", "api.tmdb.test");
            });
  }
}
