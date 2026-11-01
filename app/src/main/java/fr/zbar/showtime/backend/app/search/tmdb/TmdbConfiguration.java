package fr.zbar.showtime.backend.app.search.tmdb;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TmdbProperties.class)
class TmdbConfiguration {

  @Bean
  RestClient tmdbRestClient(RestClient.Builder builder, TmdbProperties properties) {
    return authenticated(builder, properties);
  }

  static RestClient authenticated(RestClient.Builder builder, TmdbProperties properties) {
    return builder
        .baseUrl(properties.baseUrl())
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.token())
        .build();
  }
}
