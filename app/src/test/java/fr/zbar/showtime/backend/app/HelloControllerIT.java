package fr.zbar.showtime.backend.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class HelloControllerIT {

  @Autowired private RestTestClient restTestClient;

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
