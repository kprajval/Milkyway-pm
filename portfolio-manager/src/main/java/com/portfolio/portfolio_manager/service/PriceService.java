package com.portfolio.portfolio_manager.service;

import com.portfolio.portfolio_manager.dto.PriceApiResponse;
import com.portfolio.portfolio_manager.dto.PriceResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PriceService {

    private final RestTemplate restTemplate = new RestTemplate();

    public double getCurrentPrice(String ticker) {

        String url =
                "https://c4rm9elh30.execute-api.us-east-1.amazonaws.com/default/cachedPriceData?ticker="
                        + ticker;

        PriceApiResponse response =
                restTemplate.getForObject(url, PriceApiResponse.class);

        if (response == null ||
                response.getPrice_data() == null ||
                response.getPrice_data().getClose() == null ||
                response.getPrice_data().getClose().isEmpty()) {

            System.out.println("No price data for " + ticker);
            return 0;
        }

        // ✅ LIVE PRICE = last close value
        List<Double> closes = response.getPrice_data().getClose();
        double latestPrice = closes.get(closes.size() - 1);

        System.out.println("Live price of " + ticker + " = " + latestPrice);

        return latestPrice;
    }


}
