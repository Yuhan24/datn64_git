package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Integer> {

    GioHangChiTiet findByGioHang_IdAndSanPhamChiTiet_Id(
            Integer gioHangId, Integer sanPhamChiTietId);

    List<GioHangChiTiet> findByGioHang_Id(Integer gioHangId);
}