package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Integer> {

    GioHangChiTiet findByGioHang_IdAndSanPhamChiTiet_Id(
            Integer gioHangId, Integer sanPhamChiTietId
    );

    List<GioHangChiTiet> findByGioHang_Id(Integer gioHangId);

    @Query("""
           select gct.soLuong
           from GioHangChiTiet gct
           where gct.gioHang.khachHang.id = :khachHangId
             and gct.sanPhamChiTiet.id = :sanPhamChiTietId
           """)
    Integer findSoLuongTrongGio(@Param("khachHangId") Integer khachHangId,
                                @Param("sanPhamChiTietId") Integer sanPhamChiTietId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("""
       delete from GioHangChiTiet g
       where g.gioHang.id = :gioHangId
         and g.id in :itemIds
       """)
    void deleteSelectedItems(@Param("gioHangId") Integer gioHangId,
                             @Param("itemIds") List<Integer> itemIds);
}