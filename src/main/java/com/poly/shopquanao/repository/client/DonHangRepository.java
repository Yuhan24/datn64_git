package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.DonHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonHangRepository extends JpaRepository<DonHang, Integer> {
    List<DonHang> findByKhachHang_IdOrderByNgayTaoDesc(Integer khachHangId);
    Optional<DonHang> findByMaDonHang(String maDonHang);

    List<DonHang> findByLoaiDonAndTrangThaiIdOrderByNgayTaoDesc(Integer loaiDon ,Integer trangThaiId);
    Page<DonHang> findByLoaiDonOrderByNgayTaoDesc(Integer loaiDon , Pageable pageable);
}
