package com.portfolio.portfolio_manager.dto;

public class DashboardResponse {

    private double totalValue;

    private double stockValue;
    private double bondValue;
    private double cashValue;

    private double stockPercentage;
    private double bondPercentage;
    private double cashPercentage;

    public DashboardResponse(double totalValue,
                             double stockValue,
                             double bondValue,
                             double cashValue,
                             double stockPercentage,
                             double bondPercentage,
                             double cashPercentage) {
        this.totalValue = totalValue;
        this.stockValue = stockValue;
        this.bondValue = bondValue;
        this.cashValue = cashValue;
        this.stockPercentage = stockPercentage;
        this.bondPercentage = bondPercentage;
        this.cashPercentage = cashPercentage;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public double getStockValue() {
        return stockValue;
    }

    public double getBondValue() {
        return bondValue;
    }

    public double getCashValue() {
        return cashValue;
    }

    public double getStockPercentage() {
        return stockPercentage;
    }

    public double getBondPercentage() {
        return bondPercentage;
    }

    public double getCashPercentage() {
        return cashPercentage;
    }
}
