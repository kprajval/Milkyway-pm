package com.neueda.pm_milkyway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.neueda.pm_milkyway.entity.WatchlistEntity;

@Repository
public interface WatchListRepo extends JpaRepository<WatchlistEntity, String> {
    default void saveNewStock(String stock) {
        WatchlistEntity entity = new WatchlistEntity();
        entity.setStock(stock);
        save(entity);
    }
}