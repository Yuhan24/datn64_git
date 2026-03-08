package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamChiTietADMRepository extends JpaRepository<SanPhamChiTiet,Integer> {


    //tránh trùng biến thể sản phẩm màu sắc kích cỡ

    boolean existsBySanPham_IdAndKichCo_IdAndMauSac_Id(Integer sanPhamId, Integer kichCoId, Integer mauSacId);


    List<SanPhamChiTiet> findByTrangThaiTrue();

}
