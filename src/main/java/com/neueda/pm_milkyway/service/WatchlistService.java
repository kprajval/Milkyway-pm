package com.neueda.pm_milkyway.service;

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
}
