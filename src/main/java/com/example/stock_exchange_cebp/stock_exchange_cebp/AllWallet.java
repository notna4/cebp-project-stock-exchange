package com.example.stock_exchange_cebp.stock_exchange_cebp;

import java.util.Map;

public class AllWallet {
    private Map<String, Wallet> companyWallets; // Maps companyId -> Wallet

    // Default constructor for Firebase deserialization
    public AllWallet() {}

    // Parameterized constructor
    public AllWallet(Map<String, Wallet> companyWallets) {
        this.companyWallets = companyWallets;
    }

    // Getters and Setters
    public Map<String, Wallet> getCompanyWallets() {
        return companyWallets;
    }

    public void setCompanyWallets(Map<String, Wallet> companyWallets) {
        this.companyWallets = companyWallets;
    }
}
