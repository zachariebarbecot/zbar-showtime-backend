package fr.zbar.showtime.backend.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.zbar.showtime.backend.core.domain.EmailAlreadyUsedException;
import fr.zbar.showtime.backend.core.domain.InvalidFieldException;
import io.micrometer.tracing.Tracer;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ProblemDetail;

class DomainExceptionHandlerTest {

  @SuppressWarnings("unchecked")
  private final ObjectProvider<Tracer> tracerProvider = mock(ObjectProvider.class);

  private final DomainExceptionHandler handler = new DomainExceptionHandler(tracerProvider);

  private void activeSpanWithTraceId(String traceId) {
    Tracer tracer = mock(Tracer.class, RETURNS_DEEP_STUBS);
    when(tracer.currentSpan().context().traceId()).thenReturn(traceId);
    when(tracerProvider.getIfAvailable()).thenReturn(tracer);
    Mockito.doAnswer(
            invocation -> {
              invocation.<Consumer<Tracer>>getArgument(0).accept(tracer);
              return null;
            })
        .when(tracerProvider)
        .ifAvailable(ArgumentMatchers.any());
  }

  @Test
  void should_add_trace_id_to_400_when_a_span_is_active() {
    activeSpanWithTraceId("abc123");

    ProblemDetail problem =
        handler.handleInvalidField(new InvalidFieldException("email", "Email must be valid"));

    assertThat(problem.getProperties()).containsEntry("traceId", "abc123");
  }

  @Test
  void should_add_trace_id_to_409_when_a_span_is_active() {
    activeSpanWithTraceId("abc123");

    ProblemDetail problem = handler.handleEmailAlreadyUsed(new EmailAlreadyUsedException());

    assertThat(problem.getProperties()).containsEntry("traceId", "abc123");
  }

  @Test
  void should_omit_trace_id_when_no_span_is_active() {
    ProblemDetail problem = handler.handleEmailAlreadyUsed(new EmailAlreadyUsedException());

    assertThat(problem.getProperties()).isNull();
  }
}
