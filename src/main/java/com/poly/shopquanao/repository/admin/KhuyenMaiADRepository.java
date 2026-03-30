package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KhuyenMaiADRepository extends JpaRepository<KhuyenMai,Integer> {

    boolean existsByMaKhuyenMai(String maKhuyenMai);
    Optional<KhuyenMai> findByMaKhuyenMaiIgnoreCase(String maKhuyenMai);
}
