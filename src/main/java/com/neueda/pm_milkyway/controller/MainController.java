package com.neueda.pm_milkyway.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.neueda.pm_milkyway.entity.HoldingsEntity;
import com.neueda.pm_milkyway.service.HoldingsService;
import com.neueda.pm_milkyway.service.TransactionsService;
import com.neueda.pm_milkyway.service.WatchlistService;

@RestController
public class MainController {

    private final TransactionsService transactionsService;
    private final WatchlistService watchlistService;
    private final HoldingsService holdingService;

    public MainController(TransactionsService transactionsService, WatchlistService watchlistService, HoldingsService holdingService) {
        this.transactionsService = transactionsService;
        this.watchlistService = watchlistService;
        this.holdingService = holdingService;
    }

    @GetMapping("/")
    public ModelAndView getDashboard() {
        ModelAndView mav = new ModelAndView("Dashboard");
        List<HoldingsEntity> holdings = holdingService.getAllHoldings();
        mav.addObject("holdings", holdings);
        return mav;
    }

    @GetMapping("/market-lookup")
    public ModelAndView getMarketLookup() {
        ModelAndView mav = new ModelAndView("MarketLookup");
        mav.addObject("watchlist", watchlistService.getWatchlistItems());
        return mav;
    }
    
    @GetMapping("/performance")
    public ModelAndView getPerformance() {
        ModelAndView mav = new ModelAndView("Performance");
        List<HoldingsEntity> holdings = holdingService.getAllHoldings();
        List<String> watchlist = watchlistService.getWatchlistItems();
        mav.addObject("holdings", holdings);
        mav.addObject("watchlist", watchlist);
        return mav;
    }

    @GetMapping("/transaction-history")
    public ModelAndView getTransactionHistory() {
        ModelAndView mav = new ModelAndView("TransactionHistory");
        mav.addObject("transactions", transactionsService.getAllTransactions());
        return mav;
    }
}