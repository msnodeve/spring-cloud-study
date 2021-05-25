package com.msoogle.catalogservice.service;

import com.msoogle.catalogservice.jpa.CatalogEntity;

public interface CatalogService {
    Iterable<CatalogEntity> getAllCatalogs();
}
