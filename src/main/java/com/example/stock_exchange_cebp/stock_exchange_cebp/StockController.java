package com.example.stock_exchange_cebp.stock_exchange_cebp;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/create")
    public Map<String, String> createStock(@RequestBody Map<String, Object> request) {
        Map<String, String> response = new HashMap<>();

        String userId = (String) request.get("userId");
        String companyId = (String) request.get("companyId");
        Integer pricePerShare = (Integer) request.get("pricePerShare");
        Integer sharesCounter = (Integer) request.get("sharesCounter");

        if (!isAdmin(userId)) {
            response.put("error", "You are not authorized to create a stock offer.");
            return response;
        }

        Stock stock = new Stock(companyId, pricePerShare, sharesCounter);

        String stockId = UUID.randomUUID().toString();
        DatabaseReference ref = firebaseService.getDatabase().child("stocks").child(stockId);
        ref.setValueAsync(stock);

        response.put("success", "Stock offer created successfully with ID: " + stockId);
        return response;
    }


    @PutMapping("/edit")
    public Map<String, String> editStock(@RequestBody Map<String, Object> request) {
        Map<String, String> response = new HashMap<>();

        String userId = (String) request.get("userId");
        String stockId = (String) request.get("stockId");
        Integer pricePerShare = (Integer) request.get("pricePerShare");
        Integer sharesCounter = (Integer) request.get("sharesCounter");

        if (!isAdmin(userId)) {
            response.put("error", "You are not authorized to edit the stock offer.");
            return response;
        }

        ReentrantLock lock = new ReentrantLock();

        lock.lock();
        try {
            DatabaseReference stockRef = firebaseService.getDatabase().child("stocks").child(stockId);

            Map<String, Object> updatedFields = new HashMap<>();
            if (pricePerShare != null) {
                updatedFields.put("pricePerShare", pricePerShare);
            }
            if (sharesCounter != null) {
                updatedFields.put("sharesCounter", sharesCounter);
            }

            stockRef.updateChildren(updatedFields, (error, ref) -> {
                if (error != null) {
                    response.put("error", "Error updating stock: " + error.getMessage());
                } else {
                    response.put("success", "Stock updated successfully with ID: " + stockId);
                }
            });
        } finally {
            lock.unlock();
        }

        return response;
    }

    @PostMapping("/buy")
    public CompletableFuture<Map<String, String>> buyStock(@RequestBody Map<String, Object> request) {
        CompletableFuture<Map<String, String>> futureResponse = new CompletableFuture<>();
        Map<String, String> response = new HashMap<>();

        String userId = (String) request.get("userId");
        String stockId = (String) request.get("stockId");
        Integer numberOfShares = (Integer) request.get("numberOfShares");

        if (userId == null || stockId == null || numberOfShares == null || numberOfShares <= 0) {
            response.put("error", "Invalid request: userId, stockId, and numberOfShares must be provided and valid.");
            futureResponse.complete(response);
            return futureResponse;
        }

        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try {
            DatabaseReference userRef = firebaseService.getDatabase().child("users").child(userId);

            userExists(userRef, exists -> {
                if (exists != null){
                    DatabaseReference stockRef = firebaseService.getDatabase().child("stocks").child(stockId);

                    getStockById(stockRef, stock -> {
                        if (stock != null) {
                            if (stock.getSharesCounter() < numberOfShares) {
//                                response.put("error", "Not enough shares available for purchase.");
//                                futureResponse.complete(response);
                            } else {

                                // update the stock
                                int updatedSharesCounter = stock.getSharesCounter() - numberOfShares;
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("sharesCounter", updatedSharesCounter);

                                stockRef.updateChildren(updates, (error, ref) -> {
                                    if (error != null) {
//                                        response.put("error", "Error updating stock: " + error.getMessage());
//                                        futureResponse.complete(response);
                                    } else {
//                                        response.put("success", "Purchase successful. Remaining shares: " + updatedSharesCounter);
                                    }
                                });

                                // update the user
                                int pricePerShare = stock.getPricePerShare();
                                int paidAmount = pricePerShare * numberOfShares;
                                Long currentBudget = exists.getBudget();
                                Map<String, Object> userUpdates = new HashMap<>();
                                userUpdates.put("budget", currentBudget-paidAmount);

                                String companyId = stock.getCompanyId();
                                Map<String, Object> wallet = new HashMap<>();

                                Map<String, Object> stockData = new HashMap<>();
                                stockData.put("shares", numberOfShares);
                                stockData.put("total_paid", paidAmount);

                                DatabaseReference walletRef = firebaseService.getDatabase().child("wallets").child(userId).child(companyId);
                                getWalletById(walletRef, wallet1 -> {
                                    if (wallet1 != null){
                                        System.out.println("wallet exists");
                                        Map<String, Object> existingWallet = new HashMap<>();


                                        Map<String, String> walletData = new HashMap<>();

                                        existingWallet.put("shares", String.valueOf(numberOfShares + Integer.parseInt(wallet1.getShares())));
                                        existingWallet.put("totalPaid", String.valueOf(paidAmount + Integer.parseInt(wallet1.getTotalPaid())));
                                        walletRef.updateChildren(existingWallet, (error, ref) -> {
                                            if (error != null) {
//                                                response.put("error", "Error updating user: " + error.getMessage());
//                                                futureResponse.complete(response);
                                            } else {
//                                                response.put("success", "Purchase successful. Remaining shares: " + updatedSharesCounter);
                                            }
                                        });
                                    } else {
                                        System.out.println("wallet NOT exists");

                                        Map<String, Object> newWallet = new HashMap<>();
                                        Map<String, Object> walletData = new HashMap<>();

                                        walletData.put("shares", String.valueOf(numberOfShares));
                                        walletData.put("totalPaid", String.valueOf(paidAmount));

                                        newWallet.put(companyId, walletData);

                                        walletRef.updateChildren(walletData, (error, ref) -> {
                                            if (error != null) {
//                                                response.put("error", "Error updating user: " + error.getMessage());
//                                                futureResponse.complete(response);
                                            } else {
//                                                response.put("success", "Purchase successful. Remaining shares: " + updatedSharesCounter);
                                            }
                                        });
                                    }
                                });

                                String transactionId = UUID.randomUUID().toString();
                                DatabaseReference transactionsRef = firebaseService.getDatabase().child("transactions");


                                getTransactionById(transactionsRef, transactions -> {
                                    if (transactions != null) {
                                        Map<String, Object> tran = new HashMap<>();
                                        Map<String, Object> tranData = new HashMap<>();

                                        tranData.put("userId", userId);
                                        tranData.put("companyId", companyId);
                                        tranData.put("sharesBought", numberOfShares);
                                        tranData.put("totalPaid", numberOfShares*pricePerShare);
                                        tranData.put("timestamp", System.currentTimeMillis());

                                        tran.put(transactionId, tranData);
                                        transactionsRef.updateChildren(tran, (error, ref) -> {
                                            if (error != null) {
//                                                response.put("error", "Error updating user: " + error.getMessage());
//                                                futureResponse.complete(response);
                                            } else {
//                                                response.put("success", "Transaction successful. Remaining shares: " + updatedSharesCounter);
                                            }
                                        });

                                    } else {
                                        System.out.println("eroare");
                                    }
                                });

                                userRef.updateChildren(userUpdates, (error, ref) -> {
                                    if (error != null) {
//                                        response.put("error", "Error updating user: " + error.getMessage());
//                                        futureResponse.complete(response);
                                    } else {
//                                        response.put("success", "Purchase successful. Remaining shares: " + updatedSharesCounter);
                                    }
                                });

                                response.put("success", "Purchase successful.");
                                response.put("transactionId", transactionId);
                                futureResponse.complete(response);
                            }
                        } else {
                            response.put("error", "Stock not found.");
                            futureResponse.complete(response);
                        }
                    });
                } else {
                    response.put("error", "The user does not exist.");
                    futureResponse.complete(response);
                }
            });

        } finally {
            lock.unlock();
        }

//        futureResponse.complete(response);
        return futureResponse;
    }

    public interface UserExistsCallback {
        void onResult(User exists);
    }

    public void userExists(DatabaseReference userRef, UserExistsCallback callback) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    callback.onResult(user); // Pass the retrieved stock to the callback
                } else {
                    callback.onResult(null); // Return null if no stock is found
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onResult(null); // Return false on error
            }
        });
    }

    public interface StockCallback {
        void onResult(Stock stock);
    }

    public void getStockById(DatabaseReference stockRef, StockCallback callback) {
        stockRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Stock stock = dataSnapshot.getValue(Stock.class);
                    callback.onResult(stock); // Pass the retrieved stock to the callback
                } else {
                    callback.onResult(null); // Return null if no stock is found
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onResult(null); // Return null in case of an error
            }
        });
    }



    public interface WalletCallback {
        void onResult(Wallet wallet);
    }

    public void getWalletById(DatabaseReference walletRef, WalletCallback callback) {
        walletRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Wallet wallet = dataSnapshot.getValue(Wallet.class);
                    callback.onResult(wallet); // Pass the retrieved stock to the callback
                } else {
                    callback.onResult(null); // Return null if no stock is found
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onResult(null); // Return null in case of an error
            }
        });
    }

    public interface TransactionsCallback {
        void onResult(Transactions transactions);
    }

    public void getTransactionById(DatabaseReference transactionRef, TransactionsCallback callback) {
        transactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Transactions transactions = dataSnapshot.getValue(Transactions.class);
                    callback.onResult(transactions); // Pass the retrieved stock to the callback
                } else {
                    callback.onResult(null); // Return null if no stock is found
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onResult(null); // Return null in case of an error
            }
        });
    }





    // I have to implement this after
    private boolean isAdmin(String userId) {
        return "7bb1d813-cd11-46f7-877d-ab3d3aab8f44".equals(userId);
    }
}
