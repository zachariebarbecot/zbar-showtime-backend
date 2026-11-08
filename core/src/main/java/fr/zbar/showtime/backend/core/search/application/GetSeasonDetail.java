package fr.zbar.showtime.backend.core.search.application;

public interface GetSeasonDetail {

  SeasonDetailView execute(GetSeasonDetailCommand command);
}
