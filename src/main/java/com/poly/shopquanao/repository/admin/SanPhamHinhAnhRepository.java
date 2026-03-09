package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.SanPhamHinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SanPhamHinhAnhRepository extends JpaRepository<SanPhamHinhAnh, Integer> {

    List<SanPhamHinhAnh> findBySanPham_IdOrderByIdAsc(Integer sanPhamId);

    List<SanPhamHinhAnh> findBySanPham_IdAndMauSac_IdOrderByIdAsc(Integer sanPhamId, Integer mauSacId);

    void deleteBySanPham_IdAndMauSac_Id(Integer sanPhamId, Integer mauSacId);
}