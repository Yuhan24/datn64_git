package com.poly.shopquanao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "san_pham_chi_tiet",
        uniqueConstraints = @UniqueConstraint(columnNames = {"san_pham_id", "kich_co_id", "mau_sac_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "kich_co_id", nullable = false)
    private KichCo kichCo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mau_sac_id", nullable = false)
    private MauSac mauSac;

    @Column(name = "gia_nhap", precision = 18, scale = 2)
    private BigDecimal giaNhap;

    @Column(name = "gia_ban", precision = 18, scale = 2)
    private BigDecimal giaBan;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Builder.Default
    @Column(name = "trang_thai")
    private Boolean trangThai = true;
}