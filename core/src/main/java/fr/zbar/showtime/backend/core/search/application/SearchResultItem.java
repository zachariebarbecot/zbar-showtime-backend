package fr.zbar.showtime.backend.core.search.application;

import fr.zbar.showtime.backend.core.search.domain.CatalogItem;

public record SearchResultItem(CatalogItem item, String watchlistStatus) {}
