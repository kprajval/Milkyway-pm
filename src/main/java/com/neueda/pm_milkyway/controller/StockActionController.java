package com.neueda.pm_milkyway.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neueda.pm_milkyway.entity.HoldingsEntity;
import com.neueda.pm_milkyway.service.HoldingsService;
import com.neueda.pm_milkyway.service.TransactionsService;
import com.neueda.pm_milkyway.service.WatchlistService;

@RestController
@RequestMapping("/api")
public class StockActionController {

    @Autowired
    private WatchlistService watchlistService;
    @Autowired
    private TransactionsService transactionsService;
    @Autowired
    private HoldingsService holdingsService;

    @GetMapping("/purse-value")
    public Double getPurse() {
        return transactionsService.getPurseValue();
    }

    @PostMapping("/transactions/buy")
    public ResponseEntity<String> buyStock(@RequestParam("symbol") String symbol,
            @RequestParam("quantity") int quantity,
            @RequestParam("price") double price) {
        try {
            transactionsService.executePurchase(symbol, quantity, price);
            return ResponseEntity.ok("Purchase successful");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("/watchlist/remove")
    public ResponseEntity<Void> removeFromWatchlist(@RequestParam("symbol") String symbol) {
        watchlistService.removeFromWatchlist(symbol);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/holdings/adjust")
    public ResponseEntity<String> adjustHoldings(@RequestParam("symbol") String symbol,
            @RequestParam("action") String action,
            @RequestParam("price") double price) {
        try {
            transactionsService.handleAdjustment(symbol, action, price);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/api/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        List<HoldingsEntity> holdings = holdingsService.getAllHoldings();
        double purseValue = transactionsService.getPurseValue(); // Get current balance from DB

        Map<String, Object> response = new HashMap<>();
        response.put("holdings", holdings);
        response.put("purse", purseValue);
        return ResponseEntity.ok(response);
    }
}