package com.poly.shopquanao.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "don_hang_chi_tiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonHangChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "don_hang_id", nullable = false)
    @JsonIgnoreProperties("chiTietList")
    private DonHang donHang;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_chi_tiet_id", nullable = false)
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "gia_tai_thoi_diem", precision = 18, scale = 2, nullable = false)
    private BigDecimal giaTaiThoiDiem;
}