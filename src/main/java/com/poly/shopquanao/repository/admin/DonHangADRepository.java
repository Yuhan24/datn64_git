package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.DonHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonHangADRepository extends JpaRepository<DonHang,Integer> {
        List<DonHang> findByLoaiDonAndTrangThaiIdOrderByNgayTaoDesc(Integer loaiDon ,Integer trangThaiId);

}
