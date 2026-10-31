package fr.zbar.showtime.backend.app.tmdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import fr.zbar.showtime.backend.core.domain.CatalogItem;
import fr.zbar.showtime.backend.core.domain.MediaType;
import fr.zbar.showtime.backend.core.domain.PageNumber;
import fr.zbar.showtime.backend.core.domain.SearchPage;
import fr.zbar.showtime.backend.core.domain.SearchQuery;
import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
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
        .andExpect(queryParam("query", "L'été & co"))
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
}
