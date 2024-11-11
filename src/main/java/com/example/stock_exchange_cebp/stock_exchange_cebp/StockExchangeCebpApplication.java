package com.example.stock_exchange_cebp.stock_exchange_cebp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class StockExchangeCebpApplication {

	@Autowired
	private FirebaseService firebaseService;

	public static void main(String[] args) {
		SpringApplication.run(StockExchangeCebpApplication.class, args);
	}

	@GetMapping("/test")
	public String testConnection() {
		return "Application is running and connected to Firebase!";
	}
}
