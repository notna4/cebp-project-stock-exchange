package com.example.stock_exchange_cebp.stock_exchange_cebp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private String name;
    private String status;
    private Long budget;
    private String company;
    private String password;
    private String email;
    private Boolean blocked;

    public User() {}

    public User(String id, String name, String status, Long budget, String company, String password, String email, Boolean blocked) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.budget = budget;
        this.company = company;
        this.password = password;
        this.email = email;
        this.blocked = blocked;
    }

    public Boolean getBlocked() {
        return this.blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public String getEmail() {
        return this.email;
    }

    public String getUsername() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }


    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getBudget() { return budget; }
    public void setBudget(Long budget) { this.budget = budget; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
}
