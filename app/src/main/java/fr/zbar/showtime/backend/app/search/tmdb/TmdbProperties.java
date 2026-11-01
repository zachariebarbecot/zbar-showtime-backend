package fr.zbar.showtime.backend.app.search.tmdb;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.tmdb")
public record TmdbProperties(
    String baseUrl,
    String token,
    String imageBaseUrl,
    String posterSize,
    String profileSize,
    String language) {

  String imageUrl(String size, String path) {
    return StringUtils.isEmpty(path) ? null : imageBaseUrl + "/" + size + path;
  }
}
