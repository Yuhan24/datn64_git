package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    Optional<GioHang> findByKhachHang_Id(Integer khachHangId);
}