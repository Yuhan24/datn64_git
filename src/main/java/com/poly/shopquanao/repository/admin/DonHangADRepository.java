package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.DonHang;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DonHangADRepository extends JpaRepository<DonHang,Integer> {
        List<DonHang> findByLoaiDonAndTrangThaiIdOrderByNgayTaoDesc(Integer loaiDon ,Integer trangThaiId);

        List<DonHang> findByLoaiDonOrderByNgayTaoDesc(Integer loaiDon);

        List<DonHang> findByLoaiDonAndTrangThaiIdAndNgayTaoBefore(Integer loaiDon, Integer trangThaiId, LocalDateTime time);


        Page<DonHang> findByLoaiDonOrderByNgayTaoDesc(Integer loaiDon , Pageable pageable);
}
