package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThuongHieuRepository
        extends JpaRepository<ThuongHieu, Integer> {
    List<ThuongHieu> findByTrangThaiTrue();

    boolean existsByTenThuongHieuIgnoreCase(String tenThuonghieu);

}