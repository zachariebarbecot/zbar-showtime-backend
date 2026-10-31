package fr.zbar.showtime.backend.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class JsonContentIT extends AbstractIT {

  @Test
  void should_document_only_json_request_and_response_bodies() {
    String apiDocs =
        restTestClient
            .get()
            .uri("/v3/api-docs")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

    assertThat(apiDocs).contains("\"application/json\"").doesNotContain("\"*/*\"");
  }

  @Test
  void should_return_415_when_request_body_is_not_json() {
    restTestClient
        .post()
        .uri("/auth/login")
        .contentType(MediaType.TEXT_PLAIN)
        .body("email=a@b.fr")
        .exchange()
        .expectStatus()
        .isEqualTo(415);
  }

  @Test
  void should_return_406_when_client_does_not_accept_json() {
    restTestClient
        .post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_XML)
        .body("{}")
        .exchange()
        .expectStatus()
        .isEqualTo(406);
  }
}
