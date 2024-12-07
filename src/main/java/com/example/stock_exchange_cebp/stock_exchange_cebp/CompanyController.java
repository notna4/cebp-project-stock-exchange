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

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private FirebaseService firebaseService;

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/create")
    public String createCompany(@RequestBody Company company) {
        String id = UUID.randomUUID().toString();
        company.setId(id);

        DatabaseReference ref = firebaseService.getDatabase().child("companies").child(id);
        ref.setValueAsync(company);

        return "Company created with ID: " + id;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/all")
    public CompletableFuture<Map<String, Object>> getAllCompanies() {
        DatabaseReference ref = firebaseService.getDatabase().child("companies");

        final Map<String, Object> companies = new HashMap<>();

        CompletableFuture<Map<String, Object>> futureCompanies = new CompletableFuture<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        companies.put(userSnapshot.getKey(), userSnapshot.getValue());
                    }
                } else {
                    companies.put("message", "No companies found");
                }
                futureCompanies.complete(companies);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                companies.put("error", "Error retrieving companies: " + error.getMessage());
                futureCompanies.complete(companies);
            }
        });

        return futureCompanies;
    }
}

