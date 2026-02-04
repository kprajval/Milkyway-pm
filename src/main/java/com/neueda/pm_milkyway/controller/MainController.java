package com.neueda.pm_milkyway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/milkyway")
public class MainController {

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
        return new ModelAndView("TransactionHistory");
    }
}