package com.example.stock_exchange_cebp.stock_exchange_cebp;

public class Wallet {
    private String shares;
    private String totalPaid;

    // Default constructor for Firebase deserialization
    public Wallet() {}

    // Constructor with fields
    public Wallet(String shares, String totalPaid) {
        this.shares = shares;
        this.totalPaid = totalPaid;
    }

    // Getters and Setters
    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }

    public String getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(String totalPaid) {
        this.totalPaid = totalPaid;
    }
}

