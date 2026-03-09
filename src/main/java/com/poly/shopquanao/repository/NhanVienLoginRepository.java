package com.poly.shopquanao.repository;

import com.poly.shopquanao.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienLoginRepository extends JpaRepository<NhanVien, Integer> {

    Optional<NhanVien> findByTenDangNhap(String tenDangNhap);
}

