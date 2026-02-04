package com.portfolio.portfolio_manager.service;

import com.portfolio.portfolio_manager.entity.Portfolio;
import com.portfolio.portfolio_manager.repo.PortfolioRepository;
import org.springframework.stereotype.Service;
import com.portfolio.portfolio_manager.dto.DashboardResponse;
import com.portfolio.portfolio_manager.service.PriceService;



import java.util.List;

@Service
public class PortfolioService {


    private final PortfolioRepository repository;
    private final PriceService priceService;

    public PortfolioService(PortfolioRepository repository,
                            PriceService priceService) {
        this.repository = repository;
        this.priceService = priceService;
    }

    public List<Portfolio> getAllPortfolios() {
        return repository.findAll();
    }

    public Portfolio addPortfolio(Portfolio portfolio) {
        return repository.save(portfolio);
    }

    public void deletePortfolio(Long id) {
        repository.deleteById(id);
    }
    public DashboardResponse getDashboardData() {

        double totalValue = 0;
        double stockValue = 0;
        double bondValue = 0;
        double cashValue = 0;

        for (Portfolio p : repository.findAll()) {

            double value = p.getQuantity() * p.getBuyPrice();
            totalValue += value;

            if ("STOCK".equalsIgnoreCase(p.getAssetType())) {
                stockValue += value;
            } else if ("BOND".equalsIgnoreCase(p.getAssetType())) {
                bondValue += value;
            } else if ("CASH".equalsIgnoreCase(p.getAssetType())) {
                cashValue += value;
            }
        }

        double stockPercentage = 0;
        double bondPercentage = 0;
        double cashPercentage = 0;

        if (totalValue > 0) {
            stockPercentage = (stockValue / totalValue) * 100;
            bondPercentage = (bondValue / totalValue) * 100;
            cashPercentage = (cashValue / totalValue) * 100;
        }

        return new DashboardResponse(
                totalValue,
                stockValue,
                bondValue,
                cashValue,
                stockPercentage,
                bondPercentage,
                cashPercentage
        );
    }

    public double getLivePortfolioValue() {

        double totalValue = 0;

        for (Portfolio p : repository.findAll()) {

            double currentPrice = priceService.getCurrentPrice(p.getTicker());

            if (currentPrice > 0) {
                totalValue += currentPrice * p.getQuantity();
            }
        }

        return totalValue;
    }




}
