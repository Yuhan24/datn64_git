package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    List<DonHang> findByKhachHang_IdOrderByNgayTaoDesc(Integer khachHangId);
}
