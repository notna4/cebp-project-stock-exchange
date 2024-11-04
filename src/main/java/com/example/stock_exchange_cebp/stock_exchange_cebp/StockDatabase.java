package com.example.stock_exchange_cebp.stock_exchange_cebp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StockDatabase {
    private final ConcurrentHashMap<String, StockInfo> stockData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserWallet> userWallets = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate; // Dependency injection
    private final String baseUrl = "https://msa-db-default-rtdb.europe-west1.firebasedatabase.app/companies";

    // Constructor to accept RestTemplate
    public StockDatabase(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        loadStockDataFromFirebase(); // Load data upon initialization
    }

    public void loadStockDataFromFirebase() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(baseUrl + ".json", Map.class);
            Map<String, Object> companies = response.getBody();

            if (companies != null) {
                for (Map.Entry<String, Object> entry : companies.entrySet()) {
                    String companyName = entry.getKey();
                    Map<String, Object> randomIdData = (Map<String, Object>) entry.getValue();

                    // Check if randomIdData is indeed a Map
                    if (randomIdData instanceof Map) {
                        // Iterate over the random ID map
                        for (Map.Entry<String, Object> idEntry : randomIdData.entrySet()) {
                            Object companyInfo = idEntry.getValue();

                            // Check if companyInfo is a Map
                            if (companyInfo instanceof Map) {
                                Map<String, Object> companyData = (Map<String, Object>) companyInfo;

                                // Retrieve price per share and shares from the companyData map
                                Double price = null;
                                Integer shares = null;

                                if (companyData.get("price_per_share") instanceof Number) {
                                    price = ((Number) companyData.get("price_per_share")).doubleValue();
                                }
                                if (companyData.get("shares") instanceof Number) {
                                    shares = ((Number) companyData.get("shares")).intValue();
                                }

                                // Check if both price and shares are available before adding to stockData
                                if (price != null && shares != null) {
                                    stockData.put(companyName, new StockInfo(price, shares));
                                } else {
                                    System.err.println("Data for company " + companyName + " is incomplete. Skipping.");
                                }
                            } else {
                                System.err.println("Expected a Map for company info but got: " + companyInfo.getClass().getSimpleName());
                            }
                        }
                    } else {
                        System.err.println("Expected a Map for random ID data but got: " + randomIdData.getClass().getSimpleName());
                    }
                }
            } else {
                System.err.println("No companies found in Firebase.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions as necessary
        }
    }


    public boolean updateStockPrice(String company, double price) {
        StockInfo stockInfo = stockData.get(company);
        if (stockInfo != null) {
            stockInfo.setPrice(price);
            return true;
        }
        return false;
    }

    public void addCompany(String company, double initialPrice, int shares) {
        stockData.put(company, new StockInfo(initialPrice, shares));
    }

    public double getStockPrice(String company) {
        StockInfo stockInfo = stockData.get(company);
        return stockInfo != null ? stockInfo.getPrice() : -1.0;
    }

    public int getStockShares(String company) {
        StockInfo stockInfo = stockData.get(company);
        return stockInfo != null ? stockInfo.getShares() : -1;
    }

    public boolean buyStock(String company) {
        StockInfo stockInfo = stockData.get(company);
        if (stockInfo != null && stockInfo.hasSharesAvailable()) {
            return stockInfo.decrementShares(); // Use decrementShares for atomic operation
        }
        return false;
    }

    public UserWallet getUserWallet(String userName) {
        userWallets.putIfAbsent(userName, new UserWallet(userName));
        return userWallets.get(userName);
    }

    public Map<String, StockInfo> getAllStocks() {
        return Collections.unmodifiableMap(stockData);
    }
}
