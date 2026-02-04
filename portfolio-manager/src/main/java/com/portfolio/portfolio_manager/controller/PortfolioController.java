package com.portfolio.portfolio_manager.controller;

import com.portfolio.portfolio_manager.entity.Portfolio;
import com.portfolio.portfolio_manager.service.PortfolioService;
import org.springframework.web.bind.annotation.*;
import com.portfolio.portfolio_manager.dto.DashboardResponse;


import java.util.List;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private final PortfolioService service;

    public PortfolioController(PortfolioService service) {
        this.service = service;
    }

    // View portfolio
    @GetMapping("/")
    public List<Portfolio> getPortfolio() {
        return service.getAllPortfolios();
    }

    // Add asset
    @PostMapping("/")
    public Portfolio addPortfolio(@RequestBody Portfolio portfolio) {
        return service.addPortfolio(portfolio);
    }

    // Remove asset
    @DeleteMapping("/{id}")
    public String deletePortfolio(@PathVariable Long id) {
        service.deletePortfolio(id);
        return "Portfolio entry deleted successfully";
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return service.getDashboardData();
    }

    @GetMapping("/live-value")
    public double getLivePortfolioValue() {
        return service.getLivePortfolioValue();
    }


}
