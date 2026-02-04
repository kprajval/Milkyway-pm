package com.neueda.pm_milkyway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.neueda.pm_milkyway.service.TransactionsService;
import com.neueda.pm_milkyway.service.WatchlistService;

@RestController
public class MainController {

    private final TransactionsService transactionsService;
    private final WatchlistService watchlistService;

    public MainController(TransactionsService transactionsService, WatchlistService watchlistService) {
        this.transactionsService = transactionsService;
        this.watchlistService = watchlistService;
    }

    @GetMapping("/")
    public ModelAndView getDashboard() {
        return new ModelAndView("Dashboard");
    }

    @GetMapping("/market-lookup")
    public ModelAndView getMarketLookup() {
        ModelAndView mav = new ModelAndView("MarketLookup");
        mav.addObject("watchlist", watchlistService.getWatchlistItems());
        return mav;
    }
    
    @GetMapping("/performance")
    public ModelAndView getPerformance() {
        return new ModelAndView("Performance");
    }

    @GetMapping("/transaction-history")
    public ModelAndView getTransactionHistory() {
        ModelAndView mav = new ModelAndView("TransactionHistory");
        mav.addObject("transactions", transactionsService.getAllTransactions());
        return mav;
    }
}