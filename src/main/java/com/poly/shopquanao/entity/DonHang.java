package com.poly.shopquanao.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đơn hàng – map bảng [don_hang].
 * Chứa thông tin đơn hàng: khách hàng, nhân viên xử lý,
 * tổng tiền, trạng thái, địa chỉ giao, khuyến mãi…
 */
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

    /** Mã đơn hàng duy nhất (VD: DH001) */
    @Column(name = "ma_don_hang", unique = true, nullable = false, length = 50)
    private String maDonHang;

    /** Khách hàng đặt đơn */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private KhachHang khachHang;

    /** Nhân viên xử lý đơn (có thể null nếu đơn online chưa assign) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private NhanVien nhanVien;

    /** Tổng tiền đơn hàng */
    @Column(name = "tong_tien", precision = 18, scale = 2)
    private BigDecimal tongTien;

    /** ID trạng thái: 1=Chờ xác nhận, 2=Đang giao, 3=Hoàn thành, 4=Đã hủy */
    @Builder.Default
    @Column(name = "trang_thai_id")
    private Integer trangThaiId = 1;

    /** Ngày tạo đơn – tự set nếu null */
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    /** Địa chỉ giao hàng */
    @Column(name = "dia_chi_giao_hang", length = 500)
    private String diaChiGiaoHang;

    /** Số điện thoại người nhận */
    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    /** Ghi chú đơn hàng */
    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    /** Mã khuyến mãi áp dụng (nếu có) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khuyen_mai_id")
    private KhuyenMai khuyenMai;

    /** Số tiền được giảm từ khuyến mãi */
    @Builder.Default
    @Column(name = "tien_giam", precision = 18, scale = 2)
    private BigDecimal tienGiam = BigDecimal.ZERO;

    // ====================== CỘT MỚI – SYNC VỚI DB ======================

    /** Tên người nhận hàng */
    @Column(name = "ten_nguoi_nhan", length = 100)
    private String tenNguoiNhan;

    /** Phương thức thanh toán: COD / VNPAY / MOMO */
    @Column(name = "phuong_thuc_thanh_toan", length = 50)
    private String phuongThucThanhToan;

    /** Trạng thái thanh toán: CHUA_THANH_TOAN / DA_THANH_TOAN */
    @Column(name = "trang_thai_thanh_toan", length = 50)
    private String trangThaiThanhToan;

    /** Loại đơn: true = Online, false = Offline (bán tại quầy) */
    @Column(name = "loai_don")
    private Boolean loaiDon;

    // ====================================================================

    /** Danh sách chi tiết đơn hàng */
    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties("donHang")
    private List<DonHangChiTiet> chiTietList = new ArrayList<>();

    /** Tự động set giá trị mặc định trước khi lưu vào DB */
    @PrePersist
    void prePersist() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
        if (tienGiam == null) tienGiam = BigDecimal.ZERO;
        if (trangThaiId == null) trangThaiId = 1;
    }
}