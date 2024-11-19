package com.example.stock_exchange_cebp.stock_exchange_cebp;

public class User {
    private String id;
    private String name;
    private String status;
    private String budget;
    private String company;
    private String password;

    public User() {}

    public User(String id, String name, String status, String budget, String company, String password) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.budget = budget;
        this.company = company;
        this.password = password;
    }


    public String getUsername() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
}
