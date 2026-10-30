package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.GetGreeting;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

  private final GetGreeting getGreeting;

  HelloController(GetGreeting getGreeting) {
    this.getGreeting = getGreeting;
  }

  @GetMapping("/hello")
  HelloResponse hello() {
    return new HelloResponse(getGreeting.execute().message());
  }
}
