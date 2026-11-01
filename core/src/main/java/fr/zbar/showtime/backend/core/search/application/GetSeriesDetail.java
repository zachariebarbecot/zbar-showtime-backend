package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeriesDetail;

public interface GetSeriesDetail {

  SeriesDetail execute(GetSeriesDetailCommand command);
}
