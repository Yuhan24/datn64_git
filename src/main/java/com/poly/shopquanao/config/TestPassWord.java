package com.poly.shopquanao.config;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class TestPassWord {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("1234"));
    }
}


