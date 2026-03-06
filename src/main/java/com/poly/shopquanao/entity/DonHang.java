package com.poly.shopquanao.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "don_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_don_hang", unique = true, nullable = false, length = 50)
    private String maDonHang;

    // giữ nguyên entity KhachHang theo yêu cầu bạn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private KhachHang khachHang;

    // giữ nguyên entity NhanVien theo yêu cầu bạn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private NhanVien nhanVien;

    @Column(name = "tong_tien", precision = 18, scale = 2)
    private BigDecimal tongTien;

    /**
     * Vì bạn CHƯA có bảng trang_thai_don_hang,
     * nên giữ trạng thái dạng INT đúng theo cột trang_thai_id.
     */
    @Builder.Default
    @Column(name = "trang_thai_id")
    private Integer trangThaiId = 1;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "dia_chi_giao_hang", length = 500)
    private String diaChiGiaoHang;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khuyen_mai_id")
    private KhuyenMai khuyenMai;

    @Builder.Default
    @Column(name = "tien_giam", precision = 18, scale = 2)
    private BigDecimal tienGiam = BigDecimal.ZERO;

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties("donHang")
    private List<DonHangChiTiet> chiTietList = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
        if (tienGiam == null) tienGiam = BigDecimal.ZERO;
        if (trangThaiId == null) trangThaiId = 1;
    }
}