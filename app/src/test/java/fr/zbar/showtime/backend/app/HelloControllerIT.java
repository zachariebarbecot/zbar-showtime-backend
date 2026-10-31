package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class HelloControllerIT extends AbstractIT {

  @Test
  void should_return_hello_world_message_when_getting_hello() {
    restTestClient
        .get()
        .uri("/hello")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.message")
        .isEqualTo("Hello World");
  }

  @Test
  void should_return_405_when_posting_hello() {
    restTestClient.post().uri("/hello").exchange().expectStatus().isEqualTo(405);
  }
}
