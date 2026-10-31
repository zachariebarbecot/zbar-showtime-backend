package fr.zbar.showtime.backend.app.config;

import fr.zbar.showtime.backend.core.auth.application.AuthenticateUser;
import fr.zbar.showtime.backend.core.auth.application.AuthenticateUserService;
import fr.zbar.showtime.backend.core.shared.CoreUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ComponentScan(
    basePackages = "fr.zbar.showtime.backend.core",
    useDefaultFilters = false,
    includeFilters = @Filter(type = FilterType.ANNOTATION, classes = CoreUseCase.class))
class CoreConfiguration {

  @Bean
  @Primary
  AuthenticateUser transactionalAuthenticateUser(
      AuthenticateUserService delegate, TransactionTemplate transactionTemplate) {
    return command -> transactionTemplate.execute(status -> delegate.execute(command));
  }
}
