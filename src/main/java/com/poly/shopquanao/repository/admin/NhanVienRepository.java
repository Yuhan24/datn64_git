package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    Optional<NhanVien> findByTenDangNhap(String tenDangNhap);


    /** Kiểm tra tên đăng nhập đã tồn tại chưa (dùng khi thêm mới) */
    boolean existsByTenDangNhap(String tenDangNhap);

    /** Kiểm tra mã nhân viên đã tồn tại chưa */
    boolean existsByMaNhanVien(String maNhanVien);

    /** Lọc nhân viên theo trạng thái: true = hoạt động, false = nghỉ việc */
    List<NhanVien> findByTrangThai(Boolean trangThai);
}

