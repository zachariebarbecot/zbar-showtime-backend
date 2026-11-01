package fr.zbar.showtime.backend.app.search.tmdb;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
interface TmdbSeriesApi {

  @GetExchange("/tv/{id}")
  TmdbSearchResponse.Result series(@PathVariable long id, @RequestParam String language);

  @GetExchange("/tv/{id}?append_to_response=aggregate_credits")
  TmdbSeriesDetailResponse seriesDetail(@PathVariable long id, @RequestParam String language);

  @GetExchange("/tv/{id}/season/{seasonNumber}")
  TmdbSeasonResponse seasonDetail(
      @PathVariable long id, @PathVariable int seasonNumber, @RequestParam String language);
}
