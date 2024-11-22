package com.example.stock_exchange_cebp.stock_exchange_cebp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class FirebaseService {
    private DatabaseReference database;

    @PostConstruct
    public void initialize() throws IOException {
        try {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/config/msa-db-firebase-adminsdk-i1cob-bf3faa8aea.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://msa-db-default-rtdb.europe-west1.firebasedatabase.app/")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully.");
            } else {
                System.out.println("Firebase app already initialized.");
            }

            this.database = FirebaseDatabase.getInstance().getReference();
            System.out.println("Database reference initialized successfully.");
        } catch (IOException e) {
            System.err.println("Error initializing Firebase: " + e.getMessage());
            throw new RuntimeException("Could not initialize Firebase", e);
        }
    }

    public DatabaseReference getDatabase() {
        return database;
    }
}
