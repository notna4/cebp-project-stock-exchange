package com.example.stock_exchange_cebp.stock_exchange_cebp;

public class Company {
    private String id;
    private String name;

    public Company() {}

    public Company(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
