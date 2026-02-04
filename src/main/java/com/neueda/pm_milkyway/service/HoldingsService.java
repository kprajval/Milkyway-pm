package com.neueda.pm_milkyway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.neueda.pm_milkyway.repo.HoldingsRepo;

@Service
public class HoldingsService {

    private final HoldingsRepo holdingsRepo;

    @Autowired
    public HoldingsService(HoldingsRepo holdingsRepo) {
        this.holdingsRepo = holdingsRepo;
    }

    
}
