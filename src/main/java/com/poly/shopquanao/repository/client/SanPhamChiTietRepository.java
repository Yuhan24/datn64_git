package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.SanPhamChiTiet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    @Query("""
        select ct from SanPhamChiTiet ct
        left join fetch ct.mauSac
        left join fetch ct.kichCo
        where ct.sanPham.id = :sanPhamId
    """)
    List<SanPhamChiTiet> findBySanPhamId(@Param("sanPhamId") Integer sanPhamId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SanPhamChiTiet s where s.id = :id")
    Optional<SanPhamChiTiet> findByIdForUpdate(@Param("id") Integer id);
}