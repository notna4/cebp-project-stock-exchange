package com.example.stock_exchange_cebp.stock_exchange_cebp;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserWallet {
    private final String userName;
    private final Map<String, Integer> stocks;

    public UserWallet(String userName) {
        this.userName = userName;
        this.stocks = new ConcurrentHashMap<>(); // Using ConcurrentHashMap for thread safety
    }

    // Atomically add stock to the wallet
    public void addStock(String company, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        stocks.merge(company, quantity, Integer::sum); // Add quantity atomically
    }

    public String getUserName() {
        return userName;
    }

    // Return unmodifiable view of stocks
    public Map<String, Integer> getStocks() {
        return Collections.unmodifiableMap(stocks);
    }

    // Get the quantity for a specific stock
    public int getStockQuantity(String company) {
        return stocks.getOrDefault(company, 0);
    }

    // Remove stock from the wallet with quantity check
    public void removeStock(String company, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        stocks.computeIfPresent(company, (key, val) -> {
            int newQuantity = val - quantity;
            if (newQuantity > 0) {
                return newQuantity; // Return updated quantity
            } else {
                return null; // Remove entry if quantity is zero or less
            }
        });
    }
}
