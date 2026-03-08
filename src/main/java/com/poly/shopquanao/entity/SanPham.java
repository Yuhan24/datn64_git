package com.poly.shopquanao.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "san_pham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_san_pham", unique = true, nullable = false, length = 20)
    private String maSanPham;

    @Column(name = "ten_san_pham", nullable = false, length = 255)
    private String tenSanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thuong_hieu_id")
    private ThuongHieu thuongHieu;

    @Column(name = "chat_lieu")
    private String chatLieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "danh_muc_id")
    private DanhMuc danhMuc;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Builder.Default
    @Column(name = "trang_thai")
    private Boolean trangThai = true;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnoreProperties("sanPham")
    private List<SanPhamChiTiet> chiTietList = new ArrayList<>();

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnoreProperties("sanPham")
    private Set<SanPhamHinhAnh> hinhAnhList = new HashSet<>();
//    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @Builder.Default
//    @JsonIgnoreProperties("sanPham")
//    private List<SanPhamHinhAnh> hinhAnhList = new ArrayList<>();
}