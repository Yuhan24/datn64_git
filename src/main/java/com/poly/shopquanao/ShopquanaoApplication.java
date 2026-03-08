package com.poly.shopquanao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShopquanaoApplication {

    public static void main(String[] args) {

        SpringApplication.run(ShopquanaoApplication.class, args);

        System.out.println("=====================================");
        System.out.println("🚀 Ứng dụng đang chạy tại:");
        System.out.println("👉 http://localhost:8080");
        System.out.println("=====================================");
    }
}
