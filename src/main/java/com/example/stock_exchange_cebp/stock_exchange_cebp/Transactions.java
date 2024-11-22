package com.example.stock_exchange_cebp.stock_exchange_cebp;

import java.util.Map;

public class Transactions {
    private Map<String, Transaction> transactions;

    // Default no-argument constructor (required by Firebase)
    public Transactions() {
    }

    // Parameterized constructor (optional, for convenience)
    public Transactions(Map<String, Transaction> transactions) {
        this.transactions = transactions;
    }

    // Getter and setter
    public Map<String, Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Map<String, Transaction> transactions) {
        this.transactions = transactions;
    }
}

