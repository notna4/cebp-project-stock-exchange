package com.example.stock_exchange_cebp.stock_exchange_cebp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/stock")
public class StockController {
    private final StockDatabase stockDatabase;
    private static final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Autowired
    public StockController(StockDatabase stockDatabase) {
        this.stockDatabase = stockDatabase;
    }

    @PostMapping("/buy")
    public String buyStock(@RequestBody StockBuyRequest request) {
        StringBuilder response = new StringBuilder();

        Thread buyThread1 = createBuyThread(request.getCompany1(), request.getPrice1(), request.getUser1(), response);
        Thread buyThread2 = null; // Declare buyThread2 outside the if block

        if (request.getCompany2() != null && request.getPrice2() != null && request.getUser2() != null) {
            buyThread2 = createBuyThread(request.getCompany2(), request.getPrice2(), request.getUser2(), response); // Initialize buyThread2 here
            buyThread2.start(); // Start buyThread2
        }

        buyThread1.start(); // Start buyThread1

        try {
            buyThread1.join(); // Wait for buyThread1 to finish
            if (buyThread2 != null) { // Check if buyThread2 was initialized
                buyThread2.join(); // Wait for buyThread2 to finish
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.append("Thread interrupted.");
        }

        return response.toString();
    }

    private Thread createBuyThread(String company, double price, String user, StringBuilder response) {
        return new Thread(() -> {
            lockMap.putIfAbsent(company, new ReentrantLock());
            ReentrantLock lock = lockMap.get(company);

            if (lock.tryLock()) {
                try {
                    double currentPrice = stockDatabase.getStockPrice(company);
                    int availableShares = stockDatabase.getStockShares(company);

                    if (currentPrice == price && availableShares > 0) {
                        if (stockDatabase.buyStock(company)) {
                            UserWallet userWallet = stockDatabase.getUserWallet(user);
                            userWallet.addStock(company, 1); // Add 1 share to user's wallet
                            response.append("User ").append(user).append(" successfully bought 1 share of ").append(company)
                                    .append(" at price ").append(price).append("\n");
                        }
                    } else if (availableShares == 0) {
                        response.append("No shares available for ").append(company).append(".\n");
                    } else {
                        response.append("User ").append(user).append(" failed to buy ").append(company)
                                .append(" - offered price ").append(price).append(" does not match current price ")
                                .append(currentPrice).append("\n");
                    }
                } finally {
                    lock.unlock(); // Ensure the lock is always released
                }
            } else {
                response.append("User ").append(user).append(" could not buy ").append(company)
                        .append(" - another purchase is in progress.\n");
            }
        });
    }

    @PostMapping("/wallet")
    public Map<String, Object> getUserStocks(@RequestBody Map<String, String> request) {
        String userName = request.get("userName");
        UserWallet userWallet = stockDatabase.getUserWallet(userName);
        Map<String, Object> response = new HashMap<>();
        response.put("userName", userName);
        response.put("stocks", userWallet.getStocks());
        return response;
    }

    @GetMapping("/status")
    public Map<String, StockInfo> getMarketStatus() {
        return stockDatabase.getAllStocks(); // Return the unmodifiable map of stock data
    }

    @PostMapping("/update")
    public String updateStock(@RequestBody StockUpdateRequest request) {
        StringBuilder response = new StringBuilder();

        Thread updateThread1 = createUpdateThread(request.getCompany1(), request.getPrice1(), response);
        Thread updateThread2 = null; // Declare updateThread2 outside the if block

        if (request.getCompany2() != null && request.getPrice2() != null) {
            updateThread2 = createUpdateThread(request.getCompany2(), request.getPrice2(), response); // Initialize updateThread2 here
            updateThread2.start(); // Start updateThread2
        }

        updateThread1.start(); // Start updateThread1

        try {
            updateThread1.join(); // Wait for updateThread1 to finish
            if (updateThread2 != null) { // Check if updateThread2 was initialized
                updateThread2.join(); // Wait for updateThread2 to finish
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.append("Thread interrupted.");
        }

        return response.toString();
    }

    private Thread createUpdateThread(String company, double price, StringBuilder response) {
        return new Thread(() -> {
            lockMap.putIfAbsent(company, new ReentrantLock());
            ReentrantLock lock = lockMap.get(company);

            if (lock.tryLock()) {
                try {
                    if (stockDatabase.updateStockPrice(company, price)) {
                        response.append("Stock price updated for ").append(company).append(" to ").append(price).append("\n");
                    } else {
                        response.append("Stock update failed for ").append(company).append(". Company not found.\n");
                    }
                } finally {
                    lock.unlock(); // Ensure the lock is always released
                }
            } else {
                response.append("Could not update stock for ").append(company).append(" - another update is in progress.\n");
            }
        });
    }
}
