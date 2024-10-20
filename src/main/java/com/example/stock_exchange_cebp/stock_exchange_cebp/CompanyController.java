package com.example.stock_exchange_cebp.stock_exchange_cebp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompanyController {

    @GetMapping("/welcome")
    public String welcome() {
        return "hello world";
    }

}
