package com.poly.shopquanao.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "nhan_vien")
@Data
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_nhan_vien")
    private String maNhanVien;

    @ManyToOne
    @JoinColumn(name = "vai_tro_id")
    private VaiTro vaiTro;

    @Column(name = "ten_dang_nhap")
    private String tenDangNhap;

    @Column(name = "mat_khau")
    private String matKhau;

    @Column(name = "ho_ten")
    private String hoTen;

    @Column(name = "trang_thai")
    private Boolean trangThai;
}

