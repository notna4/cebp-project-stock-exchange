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

    @PostMapping("/edit")
    public CompletableFuture<Map<Boolean, String>> editUser(@RequestBody Map<String, Object> updates) {
        String userId = (String) updates.get("id");
        final Map<Boolean, String> resp = new HashMap<>();
        CompletableFuture<Map<Boolean, String>> futureResponse = new CompletableFuture<>();

        if (userId == null) {
            resp.put(false, "User ID is missing.");
            futureResponse.complete(resp);
            return futureResponse;
        }

        DatabaseReference ref = firebaseService.getDatabase().child("users").child(userId);

        // Fetch the user to check if they exist
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    resp.put(false, "User not found.");
                    futureResponse.complete(resp);
                    return;
                }

                User user = snapshot.getValue(User.class);
                if (user == null) {
                    resp.put(false, "User data is invalid.");
                    futureResponse.complete(resp);
                    return;
                }

                // Iterate through the updates and set the fields dynamically
                for (Map.Entry<String, Object> entry : updates.entrySet()) {
                    String field = entry.getKey();
                    Object value = entry.getValue();

                    switch (field) {
                        case "name":
                            user.setName((String) value);
                            break;
                        case "status":
                            user.setStatus((String) value);
                            break;
                        case "budget":
                            user.setBudget((Long) value);
                            break;
                        case "company":
                            user.setCompany((String) value);
                            break;
                        case "password":
                            user.setPassword((String) value);
                            break;
                        case "email":
                            user.setEmail((String) value);
                            break;
                        case "blocked":
                            user.setBlocked((Boolean) value);
                            break;
                        // Add more fields as needed
                    }
                }

                // Save the updated user data back to Firebase
                ref.setValue(user, (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        resp.put(false, "Failed to update user.");
                    } else {
                        resp.put(true, "User updated successfully.");
                    }
                    futureResponse.complete(resp);
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                resp.put(false, "Database error: " + error.getMessage());
                futureResponse.complete(resp);
            }
        });

        return futureResponse;
    }


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
