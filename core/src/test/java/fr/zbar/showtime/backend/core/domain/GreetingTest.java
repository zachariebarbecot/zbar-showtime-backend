package fr.zbar.showtime.backend.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class GreetingTest {

  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID OTHER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @Test
  void should_be_equal_when_ids_are_equal() {
    Greeting greeting = new Greeting(ID, "Hello World");
    Greeting sameId = new Greeting(ID, "Bonjour");

    assertThat(greeting).isEqualTo(sameId).hasSameHashCodeAs(sameId);
  }

  @Test
  void should_not_be_equal_when_ids_differ() {
    Greeting greeting = new Greeting(ID, "Hello World");
    Greeting otherId = new Greeting(OTHER_ID, "Hello World");

    assertThat(greeting).isNotEqualTo(otherId);
  }

  @Test
  void should_expose_its_id() {
    assertThat(new Greeting(ID, "Hello World").id()).isEqualTo(ID);
  }
}
