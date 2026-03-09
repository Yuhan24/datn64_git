package com.poly.shopquanao.service.impl;

import com.poly.shopquanao.dto.client.LoginDTO;
import com.poly.shopquanao.dto.client.RegisterDTO;
import com.poly.shopquanao.entity.KhachHang;
import com.poly.shopquanao.repository.KhachHangLoginRepository;
import com.poly.shopquanao.repository.VaiTroRepository;
import com.poly.shopquanao.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KhachHangLoginRepository khachHangRepo;
    private final VaiTroRepository vaiTroRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // ================= REGISTER =================
    @Override
    public void register(RegisterDTO dto) {

        if (khachHangRepo.existsByTenDangNhap(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        KhachHang kh = new KhachHang();

        kh.setMaKhachHang("KH" + System.currentTimeMillis());
        kh.setTenDangNhap(dto.getUsername());
        kh.setMatKhau(passwordEncoder.encode(dto.getPassword()));
        kh.setHoTen(dto.getFullName());
        kh.setSoDienThoai(dto.getPhone());
        kh.setDiaChi(dto.getAddress());
        kh.setTrangThai(true);
        khachHangRepo.save(kh);
    }

    // ================= LOGIN =================
    @Override
    public void login(LoginDTO dto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsername(),
                        dto.getPassword()
                )
        );
    }
}