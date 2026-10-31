package fr.zbar.showtime.backend.app.support;

import com.jayway.jsonpath.JsonPath;
import fr.zbar.starter.test.ZbarSpringBootTest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

@ZbarSpringBootTest
@AutoConfigureRestTestClient
@Import(FixedClockConfiguration.class)
@ActiveProfiles("test")
public abstract class AbstractIT {

  protected static final String PASSWORD = "Str0ng!Passw0rd";

  @Autowired protected RestTestClient restTestClient;

  protected void registerUser(String email) {
    restTestClient
        .post()
        .uri("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Map.of(
                "lastName", "Dupont",
                "firstName", "Jean",
                "email", email,
                "password", PASSWORD,
                "birthDate", "1990-05-17",
                "gender", "MALE"))
        .exchange()
        .expectStatus()
        .isCreated();
  }

  protected String loginToken(String email) {
    byte[] body =
        restTestClient
            .post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("email", email, "password", PASSWORD))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .returnResult()
            .getResponseBody();
    return JsonPath.read(new String(body, StandardCharsets.UTF_8), "$.token");
  }
}
