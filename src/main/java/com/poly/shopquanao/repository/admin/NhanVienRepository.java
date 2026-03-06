package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    Optional<NhanVien> findByTenDangNhap(String tenDangNhap);
}

