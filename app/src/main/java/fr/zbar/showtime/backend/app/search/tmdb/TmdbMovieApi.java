package fr.zbar.showtime.backend.app.search.tmdb;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
interface TmdbMovieApi {

  @GetExchange("/movie/{id}")
  TmdbSearchResponse.Result movie(@PathVariable long id, @RequestParam String language);

  @GetExchange("/movie/{id}?append_to_response=credits")
  TmdbMovieDetailResponse movieDetail(@PathVariable long id, @RequestParam String language);
}
