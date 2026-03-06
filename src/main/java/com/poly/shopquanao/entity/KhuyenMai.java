package com.poly.shopquanao.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "khuyen_mai")
@Getter
@Setter
public class KhuyenMai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_khuyen_mai", unique = true, nullable = false)
    private String maKhuyenMai;

    @Column(name = "ten_khuyen_mai", nullable = false)
    private String tenKhuyenMai;

    // true = giảm %, false = giảm tiền
    @Column(name = "loai_giam", nullable = false)
    private Boolean loaiGiam;

    @Column(name = "gia_tri_giam", precision = 18, scale = 2, nullable = false)
    private BigDecimal giaTriGiam;

    @Column(name = "gia_tri_don_toi_thieu", precision = 18, scale = 2)
    private BigDecimal giaTriDonToiThieu;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    // Quan hệ ngược lại (optional nhưng nên có)
    @OneToMany(mappedBy = "khuyenMai")
    private List<DonHang> donHangs;
}