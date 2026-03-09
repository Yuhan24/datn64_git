package com.poly.shopquanao.service;

import com.poly.shopquanao.dto.client.LoginDTO;
import com.poly.shopquanao.dto.client.RegisterDTO;

public interface AuthService {

    void register(RegisterDTO dto);

    void login(LoginDTO dto);

}