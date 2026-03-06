package com.poly.shopquanao.dto.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDTO {
    private String fullName;
    private String username;
    private String password;
    private String phone;
    private String address;
}