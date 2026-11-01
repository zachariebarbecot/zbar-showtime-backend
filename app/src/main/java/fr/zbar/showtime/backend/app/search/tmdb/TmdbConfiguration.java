package fr.zbar.showtime.backend.app.search.tmdb;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(TmdbProperties.class)
class TmdbConfiguration {

  @Bean
  RestClient tmdbRestClient(RestClient.Builder builder, TmdbProperties properties) {
    return authenticated(builder, properties);
  }

  @Bean
  TmdbMovieApi tmdbMovieApi(RestClient tmdbRestClient) {
    return movieApi(tmdbRestClient);
  }

  @Bean
  TmdbSeriesApi tmdbSeriesApi(RestClient tmdbRestClient) {
    return seriesApi(tmdbRestClient);
  }

  static TmdbMovieApi movieApi(RestClient restClient) {
    return factory(restClient).createClient(TmdbMovieApi.class);
  }

  static TmdbSeriesApi seriesApi(RestClient restClient) {
    return factory(restClient).createClient(TmdbSeriesApi.class);
  }

  private static HttpServiceProxyFactory factory(RestClient restClient) {
    return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
  }

  static RestClient authenticated(RestClient.Builder builder, TmdbProperties properties) {
    return builder
        .baseUrl(properties.baseUrl())
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.token())
        .build();
  }
}
