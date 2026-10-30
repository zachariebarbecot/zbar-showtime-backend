package fr.zbar.showtime.backend.core.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.zbar.showtime.backend.core.domain.Greeting;
import org.junit.jupiter.api.Test;

class GetGreetingServiceTest {

  private final GetGreetingService getGreetingService = new GetGreetingService();

  @Test
  void should_return_hello_world_greeting_when_executed() {
    Greeting greeting = getGreetingService.execute();

    assertThat(greeting).extracting(Greeting::message).isEqualTo("Hello World");
  }
}
