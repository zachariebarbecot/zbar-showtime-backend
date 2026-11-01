package fr.zbar.showtime.backend.core.search.application;

public interface GetSeriesDetail {

  SeriesDetailView execute(GetSeriesDetailCommand command);
}
