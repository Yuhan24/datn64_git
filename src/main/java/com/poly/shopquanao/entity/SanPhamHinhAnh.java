package com.poly.shopquanao.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "san_pham_hinh_anh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamHinhAnh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "san_pham_id", nullable = false)
    @JsonIgnoreProperties({"chiTietList", "hinhAnhList"})
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mau_sac_id")
    private MauSac mauSac; // NULL = ảnh chung (đúng DB)

    @Column(name = "duong_dan_anh", length = 255)
    private String duongDanAnh;
}