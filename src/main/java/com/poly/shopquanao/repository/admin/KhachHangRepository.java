package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KhachHangRepository
        extends JpaRepository<KhachHang,Integer> {

    Optional<KhachHang> findByTenDangNhap(String tenDangNhap);
}

