package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý khách hàng (Admin).
 * Kế thừa JpaRepository → có sẵn: findAll, findById, save, delete…
 */
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    /** Tìm khách hàng theo tên đăng nhập */
    Optional<KhachHang> findByTenDangNhap(String tenDangNhap);

    /** Kiểm tra tên đăng nhập đã tồn tại chưa */
    boolean existsByTenDangNhap(String tenDangNhap);

    /** Kiểm tra mã khách hàng đã tồn tại chưa */
    boolean existsByMaKhachHang(String maKhachHang);

    /** Đếm tổng số khách hàng (dùng cho Dashboard) */
    long count();

    /** Lọc khách hàng theo trạng thái: true = hoạt động, false = bị khoá */
    List<KhachHang> findByTrangThai(Boolean trangThai);
}
