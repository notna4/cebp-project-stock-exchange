package com.example.stock_exchange_cebp.stock_exchange_cebp;

import java.util.HashMap;
import java.util.Map;

public class Stock {
    private String companyId;
    private int pricePerShare;
    private int sharesCounter;

    public Stock() {
    }

    public Stock(String companyId, int pricePerShare, int sharesCounter) {
        this.companyId = companyId;
        this.pricePerShare = pricePerShare;
        this.sharesCounter = sharesCounter;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public int getPricePerShare() {
        return pricePerShare;
    }

    public void setPricePerShare(int pricePerShare) {
        this.pricePerShare = pricePerShare;
    }

    public int getSharesCounter() {
        return sharesCounter;
    }

    public void setSharesCounter(int sharesCounter) {
        this.sharesCounter = sharesCounter;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("companyId", companyId);
        result.put("pricePerShare", pricePerShare);
        result.put("sharesCounter", sharesCounter);
        return result;
    }
}
