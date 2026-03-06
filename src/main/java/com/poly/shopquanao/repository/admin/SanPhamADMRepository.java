package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SanPhamADMRepository extends JpaRepository<SanPham, Integer> {
    List<SanPham> findByTenSanPhamContainingIgnoreCase(String keyword); //timf kiếm
    boolean existsByMaSanPhamIgnoreCase(String maSanPham);
    boolean existsByTenSanPhamIgnoreCase(String tenSanPham);//check trùng



}