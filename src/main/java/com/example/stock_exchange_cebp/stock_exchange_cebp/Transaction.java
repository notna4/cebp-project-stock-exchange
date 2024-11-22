package com.example.stock_exchange_cebp.stock_exchange_cebp;

public class Transaction {
    private String userId;
    private String companyId;
    private int totalPaid;
    private int sharesBought;
    private int timestamp;

    // Default no-argument constructor (required by Firebase)
    public Transaction() {
    }

    // Parameterized constructor (optional, for convenience)
    public Transaction(String userId, String companyId, int totalPaid, int sharesBought, int timestamp) {
        this.userId = userId;
        this.companyId = companyId;
        this.totalPaid = totalPaid;
        this.sharesBought = sharesBought;
        this.timestamp = timestamp;
    }

    // Public getters and setters

    public int getTimestamp() {
        return this.timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public int getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(int totalPaid) {
        this.totalPaid = totalPaid;
    }

    public int getSharesBought() {
        return sharesBought;
    }

    public void setSharesBought(int sharesBought) {
        this.sharesBought = sharesBought;
    }
}
