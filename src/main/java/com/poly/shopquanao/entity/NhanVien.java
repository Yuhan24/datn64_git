package com.poly.shopquanao.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity nhân viên – map bảng [nhan_vien].
 * Nhân viên có vai trò (ADMIN / USER / STAFF …) để phân quyền.
 */
@Entity
@Table(name = "nhan_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Mã nhân viên (VD: NV01, NV02) – unique trong DB */
    @Column(name = "ma_nhan_vien", unique = true)
    private String maNhanVien;

    /** Vai trò: ADMIN, STAFF, USER … */
    @ManyToOne
    @JoinColumn(name = "vai_tro_id")
    private VaiTro vaiTro;

    /** Họ và tên đầy đủ */
    @Column(name = "ho_ten")
    private String hoTen;

    /** Tên đăng nhập – unique, dùng để login */
    @Column(name = "ten_dang_nhap", unique = true, nullable = false)
    private String tenDangNhap;

    /** Mật khẩu đã mã hoá BCrypt */
    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    /** Số điện thoại liên hệ */
    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    /** Trạng thái: true = đang làm việc, false = nghỉ việc */
    @Column(name = "trang_thai")
    private Boolean trangThai;
}
