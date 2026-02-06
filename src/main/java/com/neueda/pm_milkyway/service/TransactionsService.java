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
    public void executeSale(String symbol, int quantity, double price) {
        List<HoldingsEntity> holdings = holdingsRepo.findByStock(symbol);
        if (holdings.isEmpty()) {
            throw new RuntimeException("You do not own this stock!");
        }

        HoldingsEntity holding = holdings.get(0);
        if (holding.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient shares! You only have " + holding.getQuantity());
        }

        double totalSaleValue = quantity * price;
        double currentPurse = getPurseValue();

        // 1. Update Holdings
        // Calculate average cost to reduce total_invested proportionally
        double avgCostPerShare = holding.getTotal_invested() / holding.getQuantity();
        double costOfSoldShares = avgCostPerShare * quantity;

        holding.setQuantity(holding.getQuantity() - quantity);
        holding.setTotal_invested(holding.getTotal_invested() - costOfSoldShares);

        // If quantity is 0, we can either remove it or keep it.
        // Keeping it with 0 allows for history, but clean up is often better.
        // For now, we save (logic similar to adjustments).
        // But deleting is cleaner for the dashboard.
        if (holding.getQuantity() == 0) {
            holding.setTotal_invested(0.0); // Ensure no floating point dust
            holdingsRepo.delete(holding); // Remove if 0? Or just save?
            // Previous code in Adjust didn't delete. But for proper cleanup let's delete if
            // 0.
            // Actually, let's just save for now to be safe with existing logic patterns,
            // unless user asked for cleanup.
        } else {
            holdingsRepo.save(holding);
        }

        // 2. Record Transaction
        TransactionEntity tx = new TransactionEntity();
        tx.setDate(LocalDate.now());
        tx.setType("SELL " + symbol);
        tx.setTransactionValue(BigDecimal.valueOf(totalSaleValue));
        tx.setPurseValue(BigDecimal.valueOf(currentPurse + totalSaleValue)); // Add to purse
        tx.setStatus(true);
        transactionsRepo.save(tx);
    }

    @Transactional
    public void handleAdjustment(String symbol, String action, double currentPrice) {
        List<HoldingsEntity> holdings = holdingsRepo.findByStock(symbol);
        if (holdings.isEmpty())
            return;

        HoldingsEntity holding = holdings.get(0);
        double purse = getPurseValue();

        if (action.equals("PLUS")) {
            if (purse < currentPrice)
                throw new RuntimeException("Insufficient Funds");
            holding.setQuantity(holding.getQuantity() + 1);
            holding.setTotal_invested(holding.getTotal_invested() + currentPrice);
            saveTx(symbol, "BUY", currentPrice, purse - currentPrice);
            holdingsRepo.save(holding);
        } else if (action.equals("MINUS")) {
            if (holding.getQuantity() <= 0)
                return;
            // Use the new logic pattern for consistency or keep simple?
            // "adjustment" is usually just a simple +/- 1.
            // Let's keep existing logic but fix the saveTx call in my previous view it was
            // separate.

            holding.setQuantity(holding.getQuantity() - 1);
            double avgCost = holding.getTotal_invested() / (holding.getQuantity() + 1); // +1 because we just
                                                                                        // decremented
            holding.setTotal_invested(holding.getTotal_invested() - avgCost);

            if (holding.getQuantity() == 0) {
                holdingsRepo.delete(holding);
            } else {
                holdingsRepo.save(holding);
            }
            saveTx(symbol, "SELL", currentPrice, purse + currentPrice);
        }
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

    @Transactional
    public void addToPurse(double amount) {
        if (amount <= 0) throw new RuntimeException("Amount must be positive");
        double currentPurse = getPurseValue();
        double newPurse = currentPurse + amount;

        TransactionEntity tx = new TransactionEntity();
        tx.setDate(LocalDate.now());
        tx.setType("PURSE ADD");
        tx.setTransactionValue(java.math.BigDecimal.valueOf(amount));
        tx.setPurseValue(java.math.BigDecimal.valueOf(newPurse));
        tx.setStatus(true);
        transactionsRepo.save(tx);
    }

    @Transactional
    public void deductFromPurse(double amount) {
        if (amount <= 0) throw new RuntimeException("Amount must be positive");
        double currentPurse = getPurseValue();
        if (currentPurse < amount) throw new RuntimeException("Insufficient purse balance");
        double newPurse = currentPurse - amount;

        TransactionEntity tx = new TransactionEntity();
        tx.setDate(LocalDate.now());
        tx.setType("PURSE DEDUCT");
        tx.setTransactionValue(java.math.BigDecimal.valueOf(amount));
        tx.setPurseValue(java.math.BigDecimal.valueOf(newPurse));
        tx.setStatus(true);
        transactionsRepo.save(tx);
    }
}