package com.neueda.pm_milkyway.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neueda.pm_milkyway.entity.HoldingsEntity;
import com.neueda.pm_milkyway.entity.TransactionEntity;
import com.neueda.pm_milkyway.repo.HoldingsRepo;
import com.neueda.pm_milkyway.repo.TransactionsRepo;

@Service
public class TransactionsService {

    private final TransactionsRepo transactionsRepo;
    private final HoldingsRepo holdingsRepo;

    @Autowired
    public TransactionsService(TransactionsRepo transactionsRepo, HoldingsRepo holdingsRepo) {
        this.transactionsRepo = transactionsRepo;
        this.holdingsRepo = holdingsRepo;
    }

    public List<TransactionEntity> getAllTransactions() {
        return transactionsRepo.findAll();
    }

    /**
     * Gets the latest purse value from the transactions table.
     * If no transactions exist, returns a default starting value (e.g., 100000.0).
     */
    public Double getPurseValue() {
        return transactionsRepo.findAll().stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .findFirst()
                .map(tx -> tx.getPurseValue().doubleValue())
                .orElse(100000.0); // Starting capital if DB is empty
    }

    @Transactional
    public void executePurchase(String symbol, int quantity, double price) {
        double totalCost = quantity * price;
        double currentPurse = getPurseValue();

        if (currentPurse < totalCost) {
            throw new RuntimeException("Insufficient Purse Value!");
        }

        // 1. Update Transactions Table
        TransactionEntity tx = new TransactionEntity();
        tx.setDate(LocalDate.now());
        tx.setType("BUY " + symbol); // Set as requested
        tx.setTransactionValue(BigDecimal.valueOf(totalCost));
        tx.setPurseValue(BigDecimal.valueOf(currentPurse - totalCost));
        tx.setStatus(true);
        transactionsRepo.save(tx);

        // 2. Update Holdings Table
        // Check if we already own this stock
        List<HoldingsEntity> existing = holdingsRepo.findByStock(symbol);
        HoldingsEntity holding;
        
        if (!existing.isEmpty()) {
            holding = existing.get(0);
            holding.setQuantity(holding.getQuantity() + quantity);
            holding.setTotal_invested(holding.getTotal_invested() + totalCost);
        } else {
            holding = new HoldingsEntity();
            holding.setStock(symbol);
            holding.setQuantity(quantity);
            holding.setTotal_invested(totalCost);
        }
        
        holdingsRepo.save(holding);
    }

    @Transactional
    public void handleAdjustment(String symbol, String action, double currentPrice) {
        List<HoldingsEntity> holdings = holdingsRepo.findByStock(symbol);
        if (holdings.isEmpty()) return;

        HoldingsEntity holding = holdings.get(0);
        double purse = getPurseValue();

        if (action.equals("PLUS")) {
            if (purse < currentPrice) throw new RuntimeException("Insufficient Funds");
            holding.setQuantity(holding.getQuantity() + 1);
            holding.setTotal_invested(holding.getTotal_invested() + currentPrice);
            saveTx(symbol, "BUY", currentPrice, purse - currentPrice);
        } else if (action.equals("MINUS")) {
            if (holding.getQuantity() <= 0) return;
            holding.setQuantity(holding.getQuantity() - 1);
            // Reduce invested value proportionally
            double avgCost = holding.getTotal_invested() / (holding.getQuantity() + 1);
            holding.setTotal_invested(holding.getTotal_invested() - avgCost);
            saveTx(symbol, "SELL", currentPrice, purse + currentPrice);
        }
        holdingsRepo.save(holding);
    }

    private void saveTx(String symbol, String type, double price, double newPurse) {
        TransactionEntity tx = new TransactionEntity();
        tx.setDate(LocalDate.now());
        tx.setType(type + " " + symbol);
        tx.setTransactionValue(BigDecimal.valueOf(price));
        tx.setPurseValue(BigDecimal.valueOf(newPurse));
        tx.setStatus(true);
        transactionsRepo.save(tx);
    }

    // Inside TransactionsService.java
    public Map<String, Object> getPortfolioStats(List<HoldingsEntity> holdings, Map<String, Double> currentPrices) {
        double totalInvested = holdings.stream().mapToDouble(HoldingsEntity::getTotal_invested).sum();
        double totalMarketValue = holdings.stream()
            .mapToDouble(h -> h.getQuantity() * currentPrices.getOrDefault(h.getStock(), 0.0))
            .sum();
        
        double purse = getPurseValue(); // Fetch from your Transaction/Purse table
        double profitLoss = totalMarketValue - totalInvested;
        double percentageChange = totalInvested == 0 ? 0 : (profitLoss / totalInvested) * 100;

        Map<String, Object> stats = new HashMap<>();
        stats.put("purse", purse);
        stats.put("portfolioValue", totalMarketValue + purse); // Total wealth
        stats.put("profitLoss", profitLoss);
        stats.put("changePercent", percentageChange);
        return stats;
    }
}