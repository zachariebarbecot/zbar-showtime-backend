package fr.zbar.showtime.backend.app.search.tmdb;

import fr.zbar.showtime.backend.core.search.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.search.domain.TitleNotFoundException;
import fr.zbar.starter.restclient.RestClientClientErrorException;
import java.time.format.DateTimeParseException;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

final class TmdbCalls {

  private TmdbCalls() {}

  static <T, R> R fetch(Supplier<T> call, Function<T, R> mapper) {
    try {
      T body = call.get();
      if (body == null) {
        throw new CatalogUnavailableException(
            new IllegalStateException("Empty TMDB response body"));
      }
      return mapper.apply(body);
    } catch (RestClientClientErrorException exception) {
      if (HttpStatus.NOT_FOUND.equals(exception.statusCode())) {
        throw new TitleNotFoundException();
      }
      throw new CatalogUnavailableException(exception);
    } catch (RestClientException | DateTimeParseException exception) {
      throw new CatalogUnavailableException(exception);
    }
  }
}
