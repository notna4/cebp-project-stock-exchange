package com.example.stock_exchange_cebp.stock_exchange_cebp;

import com.google.firebase.database.DatabaseReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/create")
    public String createCompany(@RequestBody Company company) {
        String id = UUID.randomUUID().toString();
        company.setId(id);

        DatabaseReference ref = firebaseService.getDatabase().child("companies").child(id);
        ref.setValueAsync(company);

        return "Company created with ID: " + id;
    }
}

