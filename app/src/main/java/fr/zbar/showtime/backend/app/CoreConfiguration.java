package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.GetGreeting;
import fr.zbar.showtime.backend.core.application.GetGreetingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CoreConfiguration {

  @Bean
  GetGreeting getGreeting() {
    return new GetGreetingService();
  }
}
