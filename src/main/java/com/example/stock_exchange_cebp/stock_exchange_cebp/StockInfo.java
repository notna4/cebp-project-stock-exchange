package com.example.stock_exchange_cebp.stock_exchange_cebp;

import java.util.concurrent.atomic.AtomicInteger;

public class StockInfo {
    private double price;
    private final AtomicInteger shares;

    public StockInfo(double price, int shares) {
        this.price = price;
        this.shares = new AtomicInteger(shares);
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getShares() {
        return shares.get();
    }

    public void setShares(int shares) {
        this.shares.set(shares);
    }

    public boolean hasSharesAvailable() {
        return shares.get() > 0;
    }

    public boolean decrementShares() {
        // Atomically decrement shares and check if shares are available
        int currentShares;
        do {
            currentShares = shares.get();
            if (currentShares <= 0) {
                return false; // No shares available to decrement
            }
        } while (!shares.compareAndSet(currentShares, currentShares - 1)); // Atomic decrement
        return true; // Successfully decremented
    }
}
