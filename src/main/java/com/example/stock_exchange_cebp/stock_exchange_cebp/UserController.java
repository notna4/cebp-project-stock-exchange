package com.example.stock_exchange_cebp.stock_exchange_cebp;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/login")
    public CompletableFuture<Map<Boolean, String>> loginUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        final Map<Boolean, String> resp = new HashMap<>();

        if (username == null || password == null) {
            CompletableFuture<Map<Boolean, String>> future = new CompletableFuture<>();
            resp.put(false, "No id");
            future.complete(resp);
            return future;
        }

        DatabaseReference ref = firebaseService.getDatabase().child("users");
        CompletableFuture<Map<Boolean, String>> loginResult = new CompletableFuture<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean found = false;

                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && username.equals(user.getUsername()) && password.equals(user.getPassword())) {
                            resp.put(true, user.getId());
                            found = true;
                            break;
                        }
                    }
                }

                loginResult.complete(resp);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                resp.put(false, "No id.");
                loginResult.complete(resp); // Return false if there's an error during the database operation
            }
        });

        return loginResult;
    }


    @PostMapping("/create")
    public String createUser(@RequestBody User user) {
        String id = UUID.randomUUID().toString();
        user.setId(id);

        DatabaseReference ref = firebaseService.getDatabase().child("users").child(id);
        ref.setValueAsync(user);

        return "User created with id: " + id;
    }

    @GetMapping("/all")
    public CompletableFuture<Map<String, Object>> getAllUsers() {
        DatabaseReference ref = firebaseService.getDatabase().child("users");

        final Map<String, Object> users = new HashMap<>();

        CompletableFuture<Map<String, Object>> futureUsers = new CompletableFuture<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        users.put(userSnapshot.getKey(), userSnapshot.getValue());
                    }
                } else {
                    users.put("message", "No users found");
                }
                futureUsers.complete(users);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                users.put("error", "Error retrieving users: " + error.getMessage());
                futureUsers.complete(users);
            }
        });

        return futureUsers;
    }
}
