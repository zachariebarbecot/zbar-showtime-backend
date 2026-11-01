package fr.zbar.showtime.backend.app.search.tmdb;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

final class TmdbRestClientBuilders {

  private TmdbRestClientBuilders() {}

  static RestClient.Builder bootBuilder() {
    AtomicReference<RestClient.Builder> builder = new AtomicReference<>();
    new ApplicationContextRunner()
        .withPropertyValues("spring.application.name=zbar-showtime-backend")
        .withConfiguration(
            AutoConfigurations.of(
                org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration.class,
                fr.zbar.starter.restclient.RestClientAutoConfiguration.class))
        .run(context -> builder.set(context.getBean(RestClient.Builder.class)));
    return builder.get();
  }
}
