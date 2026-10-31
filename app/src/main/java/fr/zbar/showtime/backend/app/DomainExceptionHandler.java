package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.domain.CatalogUnavailableException;
import fr.zbar.showtime.backend.core.domain.EmailAlreadyUsedException;
import fr.zbar.showtime.backend.core.domain.InvalidCredentialsException;
import fr.zbar.showtime.backend.core.domain.InvalidFieldException;
import fr.zbar.showtime.backend.core.domain.UserNotFoundException;
import io.micrometer.tracing.Tracer;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RequiredArgsConstructor
class DomainExceptionHandler {

  private final ObjectProvider<Tracer> tracer;

  @ExceptionHandler(InvalidFieldException.class)
  ProblemDetail handleInvalidField(InvalidFieldException exception) {
    ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid request content.");
    List<Map<String, String>> errors =
        exception.messages().stream()
            .map(message -> Map.of("field", exception.field(), "message", message))
            .toList();
    problem.setProperty("errors", errors);
    return problem;
  }

  @ExceptionHandler(EmailAlreadyUsedException.class)
  ProblemDetail handleEmailAlreadyUsed(EmailAlreadyUsedException exception) {
    return problem(HttpStatus.CONFLICT, exception.getMessage());
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception) {
    return problem(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  @ExceptionHandler(UserNotFoundException.class)
  ProblemDetail handleUserNotFound(UserNotFoundException exception) {
    return problem(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  @ExceptionHandler(CatalogUnavailableException.class)
  ProblemDetail handleCatalogUnavailable(CatalogUnavailableException exception) {
    log.warn("Catalog unavailable", exception);
    return problem(HttpStatus.BAD_GATEWAY, "The catalog is temporarily unavailable.");
  }

  private ProblemDetail problem(HttpStatus status, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    tracer.ifAvailable(
        availableTracer -> {
          var span = availableTracer.currentSpan();
          if (span != null) {
            problem.setProperty("traceId", span.context().traceId());
          }
        });
    return problem;
  }
}
