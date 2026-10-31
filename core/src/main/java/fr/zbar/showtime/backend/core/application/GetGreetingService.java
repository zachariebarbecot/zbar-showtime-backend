package fr.zbar.showtime.backend.core.application;

import fr.zbar.showtime.backend.core.domain.Greeting;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;

@CoreUseCase
public class GetGreetingService implements GetGreeting {

  @Override
  public Greeting execute() {
    return new Greeting("Hello World");
  }
}
