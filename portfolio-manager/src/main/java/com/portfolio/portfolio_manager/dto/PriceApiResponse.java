package com.portfolio.portfolio_manager.dto;

public class PriceApiResponse {
    private String ticker;
    private PriceData price_data;

    public String getTicker() {
        return ticker;
    }

    public PriceData getPrice_data() {
        return price_data;
    }
}
