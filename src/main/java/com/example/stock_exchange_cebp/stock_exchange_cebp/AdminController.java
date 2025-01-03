package com.example.stock_exchange_cebp.stock_exchange_cebp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController{
    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/users/{id}/block")
    public ResponseEntity<String> blockUser(@PathVariable String id) {
        try {
            firebaseService.blockUser(id);
            return ResponseEntity.ok("User blocked successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error blocking user: " + e.getMessage());
        }
    }

    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<String> unblockUser(@PathVariable String id) {
        try {
            firebaseService.unblockUser(id);
            return ResponseEntity.ok("User unblocked successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error unblocking user: " + e.getMessage());
        }
    }
}
