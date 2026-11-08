package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;
import java.time.Instant;
import java.util.Map;

public record SeasonDetailView(SeasonDetail season, Map<Integer, Instant> watchedAtByEpisode) {}
