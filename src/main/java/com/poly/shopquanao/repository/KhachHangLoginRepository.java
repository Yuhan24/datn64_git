package com.poly.shopquanao.repository;

import com.poly.shopquanao.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KhachHangLoginRepository
        extends JpaRepository<KhachHang, Integer> {

    Optional<KhachHang> findByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhap(String tenDangNhap);
}