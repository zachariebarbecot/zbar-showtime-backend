package fr.zbar.showtime.backend.app;

import fr.zbar.showtime.backend.core.application.CoreUseCase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
    basePackages = "fr.zbar.showtime.backend.core",
    useDefaultFilters = false,
    includeFilters = @Filter(type = FilterType.ANNOTATION, classes = CoreUseCase.class))
class CoreConfiguration {}
