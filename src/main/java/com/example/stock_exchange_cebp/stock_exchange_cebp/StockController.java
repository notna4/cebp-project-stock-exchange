package com.example.stock_exchange_cebp.stock_exchange_cebp;

import com.google.firebase.database.DatabaseReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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


    // I have to implement this after
    private boolean isAdmin(String userId) {
        return "7bb1d813-cd11-46f7-877d-ab3d3aab8f44".equals(userId);
    }
}
