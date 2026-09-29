package com.study.microservices.product;

import com.study.microservices.product.model.Product;
import com.study.microservices.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class ProductServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(ProductRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Product(null, "iPhone 16 Pro Max", "Apple flagship smartphone 256GB", new BigDecimal("1299.99"), 50));
                repository.save(new Product(null, "MacBook Pro M3 Max", "16-inch 36GB RAM 1TB SSD", new BigDecimal("3499.99"), 20));
                repository.save(new Product(null, "Sony WH-1000XM5", "Wireless Noise-Canceling Headphones", new BigDecimal("399.99"), 100));
                log.info("Initialized demo products in H2 Database successfully!");
            }
        };
    }
}
