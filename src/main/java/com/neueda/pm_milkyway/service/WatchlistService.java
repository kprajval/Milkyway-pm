package com.neueda.pm_milkyway.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.neueda.pm_milkyway.repo.WatchListRepo;

@Service
public class WatchlistService {
    
    private final WatchListRepo watchListRepo;

    @Autowired
    public WatchlistService(WatchListRepo watchListRepo) {
        this.watchListRepo = watchListRepo;
    }

    public List<String> getWatchlistItems() {
        return watchListRepo.findAll().stream().map(item -> item.getStock()).toList();
    }

    public void addToWatchList(String stock){
        watchListRepo.saveNewStock(stock);
    }

    public void removeFromWatchlist(String stock) {
        var item = watchListRepo.findAll().stream()
            .filter(watchlistItem -> watchlistItem.getStock().equalsIgnoreCase(stock))
            .findFirst();
        item.ifPresent(watchListRepo::delete);
    }
}
