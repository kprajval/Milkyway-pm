package com.neueda.pm_milkyway.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.neueda.pm_milkyway.entity.HoldingsEntity;
import com.neueda.pm_milkyway.repo.HoldingsRepo;

@Service
public class HoldingsService {

    private final HoldingsRepo holdingsRepo;

    @Autowired
    public HoldingsService(HoldingsRepo holdingsRepo) {
        this.holdingsRepo = holdingsRepo;
    }

    public List<HoldingsEntity> getAllHoldings() {
        return holdingsRepo.findAll();
    }

    public List<HoldingsEntity> getStockHoldings(String stock) {
        return holdingsRepo.findByStock(stock);
    }
}
