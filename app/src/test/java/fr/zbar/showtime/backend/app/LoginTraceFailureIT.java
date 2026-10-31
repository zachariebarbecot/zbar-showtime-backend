package fr.zbar.showtime.backend.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import fr.zbar.showtime.backend.app.support.AbstractIT;
import fr.zbar.showtime.backend.core.domain.LoginHistory;
import fr.zbar.showtime.backend.core.domain.TokenIssuer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class LoginTraceFailureIT extends AbstractIT {

  private static final String PASSWORD = "Str0ng!Passw0rd";
  private static final String TECHNICAL_DETAIL = "secret-technical-detail";

  @Autowired private JdbcTemplate jdbcTemplate;
  @MockitoSpyBean private LoginHistory loginHistory;
  @MockitoSpyBean private TokenIssuer tokenIssuer;

  private void register(String email) {
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

  private String failedLoginBody(String email) {
    byte[] body =
        restTestClient
            .post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("email", email, "password", PASSWORD))
            .exchange()
            .expectStatus()
            .isEqualTo(500)
            .expectBody()
            .returnResult()
            .getResponseBody();
    return new String(body, StandardCharsets.UTF_8);
  }

  private int loginCount(String email) {
    return jdbcTemplate.queryForObject(
        "select count(*) from user_login l join users u on u.id = l.user_id where u.email = ?",
        Integer.class,
        email);
  }

  @Test
  void should_return_500_without_detail_nor_token_when_the_trace_cannot_be_written() {
    register("trace-ca5a@mail.fr");
    doThrow(new IllegalStateException(TECHNICAL_DETAIL)).when(loginHistory).record(any(), any());

    String body = failedLoginBody("trace-ca5a@mail.fr");

    assertThat(body).doesNotContain(TECHNICAL_DETAIL).doesNotContain("IllegalStateException");
    assertThat(body).doesNotContain("token");
    assertThat(loginCount("trace-ca5a@mail.fr")).isZero();
  }

  @Test
  void should_leave_no_trace_when_the_login_fails_after_the_trace_is_written() {
    register("trace-ca5b@mail.fr");
    doThrow(new IllegalStateException(TECHNICAL_DETAIL)).when(tokenIssuer).issue(any());

    String body = failedLoginBody("trace-ca5b@mail.fr");

    assertThat(body).doesNotContain(TECHNICAL_DETAIL);
    assertThat(loginCount("trace-ca5b@mail.fr")).isZero();
  }
}
