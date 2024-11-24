package com.example.stock_exchange_cebp.stock_exchange_cebp;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/create")
    public CompletableFuture<Map<String, String>> createStock(@RequestBody Map<String, Object> request) {
        Map<String, String> response = new HashMap<>();
        CompletableFuture<Map<String, String>> futureResponse = new CompletableFuture<>();

        String userId = (String) request.get("userId");
        String companyId = (String) request.get("companyId");
        Integer pricePerShare = (Integer) request.get("pricePerShare");
        Integer sharesCounter = (Integer) request.get("sharesCounter");

        DatabaseReference userRef = firebaseService.getDatabase().child("users").child(userId);

        getUserById(userRef, user -> {
            if (user != null && user.getStatus().equals("admin")) {

                Stock stock = new Stock(companyId, pricePerShare, sharesCounter);

                String stockId = UUID.randomUUID().toString();
                DatabaseReference ref = firebaseService.getDatabase().child("stocks").child(stockId);
                ref.setValueAsync(stock);

                response.put("success", "Stock offer created successfully with ID: " + stockId);
                futureResponse.complete(response);
            } else {
                response.put("error", "You are not authorized to create a stock offer.");
                futureResponse.complete(response);

            }
        });
        return futureResponse;
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


    @GetMapping("/all")
    public CompletableFuture<Map<String, Object>> getAllStocks() {
        DatabaseReference ref = firebaseService.getDatabase().child("stocks");

        final Map<String, Object> stocks = new HashMap<>();

        CompletableFuture<Map<String, Object>> futureStocks = new CompletableFuture<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        stocks.put(userSnapshot.getKey(), userSnapshot.getValue());
                    }
                } else {
                    stocks.put("message", "No users found");
                }
                futureStocks.complete(stocks);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                stocks.put("error", "Error retrieving users: " + error.getMessage());
                futureStocks.complete(stocks);
            }
        });

        return futureStocks;
    }

    @PostMapping("/wallet")
    public CompletableFuture<Map<String, Object>> getWallet(@RequestBody Map<String, Object> request) {
        CompletableFuture<Map<String, Object>> futureResponse = new CompletableFuture<>();
        Map<String, Object> response = new HashMap<>();
        String userId = (String) request.get("userId");

        if (userId == null || userId.isEmpty()) {
            response.put("error", "Invalid userId provided.");
            futureResponse.complete(response);
            return futureResponse;
        }

        DatabaseReference walletRef = firebaseService.getDatabase().child("wallets").child(userId);

        getAllWalletById(walletRef, wallet -> {
            if (wallet != null) {
                response.put("success", true);
                response.put("wallet", wallet);
                futureResponse.complete(response);
            } else {
                response.put("error", "Wallet not found for the given userId.");
                futureResponse.complete(response);
            }
        });

        return futureResponse;
    }

    @GetMapping("/getAllTransactions")
    public CompletableFuture<Map<String, Object>> getAllTransactions() {
        DatabaseReference ref = firebaseService.getDatabase().child("transactions");

        final Map<String, Object> transactions = new HashMap<>();

        CompletableFuture<Map<String, Object>> futureTransactions = new CompletableFuture<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        transactions.put(userSnapshot.getKey(), userSnapshot.getValue());
                    }
                } else {
                    transactions.put("message", "No transactions found");
                }
                futureTransactions.complete(transactions);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                transactions.put("error", "Error retrieving transactions: " + error.getMessage());
                futureTransactions.complete(transactions);
            }
        });

        return futureTransactions;
    }

    @PostMapping("/lastXTransactions")
    public List<Map<String, Object>> getLastXTransactions(@RequestBody Map<String, Object> request) throws ExecutionException, InterruptedException {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("transactions");
        int number = (int) request.get("numberToGet");
        Query query = databaseRef.orderByChild("timestamp").limitToLast(number);

        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Map<String, Object>> items = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    items.add((Map<String, Object>) snapshot.getValue());
                }
                Collections.reverse(items); // Reverse to get descending order
                future.complete(items);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future.get(); // Wait for Firebase query to complete
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

    public interface UserCallback {
        void onResult(User user);
    }

    public void getUserById(DatabaseReference stockRef, UserCallback callback) {
        stockRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                callback.onResult(null); // Return null in case of an error
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

    // not working yet
    public interface AllWalletCallback {
        void onResult(Map<String, Wallet> companyWallets);
    }

    public void getAllWalletById(DatabaseReference walletRef, AllWalletCallback callback) {
        walletRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Wallet> auxWallet = (Map<String, Wallet>) dataSnapshot.getValue();

//                    AllWallet allWallet = dataSnapshot.getValue(AllWallet.class);
                    callback.onResult(auxWallet); // Pass the retrieved stock to the callback
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
