package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KhuyenMaiClientRepository extends JpaRepository<KhuyenMai, Integer> {
    Optional<KhuyenMai> findByMaKhuyenMai(String maKhuyenMai);
}