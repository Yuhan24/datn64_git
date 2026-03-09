package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MauSacRepository extends JpaRepository<MauSac,Integer> {
    List<MauSac> findByTrangThai(Boolean trangThai);
    boolean existsByTenMauIgnoreCase(String tenMau);
    List<MauSac> findByTrangThaiTrue();
}
