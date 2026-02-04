package com.neueda.pm_milkyway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.neueda.pm_milkyway.service.TransactionsService;

@RestController
public class MainController {

    private final TransactionsService transactionsService;

    public MainController(TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
    }

    @GetMapping("/")
    public ModelAndView getDashboard() {
        return new ModelAndView("Dashboard");
    }

    @GetMapping("/market-lookup")
    public ModelAndView getMarketLookup() {
        return new ModelAndView("MarketLookup");
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