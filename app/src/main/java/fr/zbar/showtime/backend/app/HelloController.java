package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.GetGreeting;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class HelloController {

  private final GetGreeting getGreeting;

  @GetMapping("/hello")
  HelloResponse hello() {
    return new HelloResponse(getGreeting.execute().message());
  }
}
