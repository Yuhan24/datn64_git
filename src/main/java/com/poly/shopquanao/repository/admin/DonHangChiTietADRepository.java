package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.DonHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonHangChiTietADRepository extends JpaRepository<DonHangChiTiet,Integer > {
    List<DonHangChiTiet> findByDonHang_Id(Integer donHangId);
    Optional<DonHangChiTiet> findByDonHang_IdAndSanPhamChiTiet_Id(Integer donHangId, Integer spctId);





}
