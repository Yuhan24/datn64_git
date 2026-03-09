package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    @Query("""
        select ct from SanPhamChiTiet ct
        left join fetch ct.mauSac
        left join fetch ct.kichCo
        where ct.sanPham.id = :sanPhamId
    """)
    List<SanPhamChiTiet> findBySanPhamId(@Param("sanPhamId") Integer sanPhamId);
}