package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.SeasonDetail;

public interface GetSeasonDetail {

  SeasonDetail execute(GetSeasonDetailCommand command);
}
