package com.example.stock_exchange_cebp.stock_exchange_cebp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class StockExchangeCebpApplication {

	private static final String FIREBASE_URL = "https://msa-db-default-rtdb.europe-west1.firebasedatabase.app/companies/";

	public static void main(String[] args) throws IOException {
		SpringApplication.run(StockExchangeCebpApplication.class, args);
	}

	@Bean
	public StockDatabase stockDatabase(RestTemplate restTemplate) {
		StockDatabase stockDatabase = new StockDatabase(restTemplate); // Pass the RestTemplate to StockDatabase
		return stockDatabase;
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@PostMapping("/add-company")
	public String addCompany(@RequestBody Company company) {
		// Construct the Firebase endpoint URL for the company
		String url = "https://msa-db-default-rtdb.europe-west1.firebasedatabase.app/companies/" + company.getName() + ".json";

		// Create headers
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		// Create the request body
		Map<String, Object> data = new HashMap<>();
		data.put("name", company.getName());
		data.put("price_per_share", company.getPrice());
		data.put("shares", company.getShares());

		// Create the request entity
		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(data, headers);

		try {
			ResponseEntity<String> response = restTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);

			// Check response status and return appropriate message
			if (response.getStatusCode().is2xxSuccessful()) {
				return "Company updated successfully: " + company.getName();
			} else {
				return "Error updating company: " + response.getBody();
			}
		} catch (Exception e) {
			e.printStackTrace(); // Log the exception
			return "Error updating company: " + e.getMessage(); // Return the error message
		}
	}



	public static class Company {
		private String name;
		private double price;  // Changed to double
		private int shares;

		// Getters and Setters
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public double getPrice() { return price; }
		public void setPrice(double price) { this.price = price; }
		public int getShares() { return shares; }
		public void setShares(int shares) { this.shares = shares; }
	}
}
