package com.poly.shopquanao.test;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class EncodePassword {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String raw = "123456"; // mật khẩu gốc
        String hash = encoder.encode(raw);

        System.out.println("BCrypt = " + hash);
    }
}


