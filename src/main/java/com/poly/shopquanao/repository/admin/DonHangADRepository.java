package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.DonHang;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface DonHangADRepository extends JpaRepository<DonHang,Integer> {
    List<DonHang> findByLoaiDonAndTrangThaiIdOrderByNgayTaoDesc(Integer loaiDon ,Integer trangThaiId);

    List<DonHang> findByLoaiDonOrderByNgayTaoDesc(Integer loaiDon);

    List<DonHang> findByLoaiDonAndTrangThaiIdAndNgayTaoBefore(Integer loaiDon, Integer trangThaiId, LocalDateTime time);


    Page<DonHang> findByLoaiDonOrderByNgayTaoDesc(Integer loaiDon , Pageable pageable);

    List<DonHang> findByLoaiDonAndTrangThaiThanhToanAndNgayTaoBefore(
            Integer loaiDon,
            String trangThaiThanhToan,
            LocalDateTime time
    );


    // ==================== TỔNG QUAN ====================

    /** Tổng doanh thu tất cả đơn */
    @Query("SELECT COALESCE(SUM(d.tongTien), 0) FROM DonHang d")
    BigDecimal sumTongTien();

    /** Doanh thu đơn hoàn thành (trạng thái = 3) */
    @Query("SELECT COALESCE(SUM(d.tongTien), 0) FROM DonHang d WHERE d.trangThaiId = 3")
    BigDecimal sumDoanhThuHoanThanh();

    /** Đếm đơn theo trạng thái */
    long countByTrangThaiId(Integer trangThaiId);

    /** 5 đơn mới nhất */
    @Query("SELECT d FROM DonHang d ORDER BY d.ngayTao DESC")
    List<DonHang> findTop5ByOrderByNgayTaoDesc();

    // ==================== LỌC THEO KHOẢNG NGÀY ====================

    /**
     * Đếm tổng đơn hàng trong khoảng ngày.
     * @param tuNgay  ngày bắt đầu (inclusive)
     * @param denNgay ngày kết thúc (inclusive)
     */
    @Query("SELECT COUNT(d) FROM DonHang d WHERE d.ngayTao >= :tuNgay AND d.ngayTao <= :denNgay")
    long countByDateRange(@Param("tuNgay") LocalDateTime tuNgay,
                          @Param("denNgay") LocalDateTime denNgay);

    /**
     * Tổng doanh thu trong khoảng ngày.
     */
    @Query("SELECT COALESCE(SUM(d.tongTien), 0) FROM DonHang d WHERE d.ngayTao >= :tuNgay AND d.ngayTao <= :denNgay")
    BigDecimal sumDoanhThuByDateRange(@Param("tuNgay") LocalDateTime tuNgay,
                                      @Param("denNgay") LocalDateTime denNgay);

    /**
     * Doanh thu đơn hoàn thành trong khoảng ngày.
     */
    @Query("SELECT COALESCE(SUM(d.tongTien), 0) FROM DonHang d WHERE d.trangThaiId = 3 AND d.ngayTao >= :tuNgay AND d.ngayTao <= :denNgay")
    BigDecimal sumDoanhThuHoanThanhByDateRange(@Param("tuNgay") LocalDateTime tuNgay,
                                               @Param("denNgay") LocalDateTime denNgay);

    /**
     * Đếm đơn theo trạng thái trong khoảng ngày.
     */
    @Query("SELECT COUNT(d) FROM DonHang d WHERE d.trangThaiId = :trangThaiId AND d.ngayTao >= :tuNgay AND d.ngayTao <= :denNgay")
    long countByTrangThaiIdAndDateRange(@Param("trangThaiId") Integer trangThaiId,
                                        @Param("tuNgay") LocalDateTime tuNgay,
                                        @Param("denNgay") LocalDateTime denNgay);

    /**
     * Lấy tất cả đơn hàng trong khoảng ngày, sắp xếp theo ngày tạo giảm dần.
     * Dùng cho bảng chi tiết đơn hàng trong trang Thống kê.
     */
    @Query("SELECT d FROM DonHang d WHERE d.ngayTao >= :tuNgay AND d.ngayTao <= :denNgay ORDER BY d.ngayTao DESC")
    List<DonHang> findByDateRange(@Param("tuNgay") LocalDateTime tuNgay,
                                  @Param("denNgay") LocalDateTime denNgay);

    // ==================== DOANH THU THEO NGÀY (GROUP BY) ====================

    /**
     * Doanh thu group theo ngày (cho biểu đồ cột).
     * Trả về List<Object[]> với mỗi phần tử = [ngay (String "yyyy-MM-dd"), tongTien (BigDecimal)]
     *
     * Dùng CAST(d.ngayTao AS DATE) (SQL Server) → Thymeleaf-compatible.
     */
    @Query(value = "SELECT CAST(d.ngay_tao AS DATE) AS ngay, COALESCE(SUM(d.tong_tien), 0) AS tong " +
            "FROM don_hang d " +
            "WHERE d.ngay_tao >= :tuNgay AND d.ngay_tao <= :denNgay " +
            "GROUP BY CAST(d.ngay_tao AS DATE) " +
            "ORDER BY ngay",
            nativeQuery = true)
    List<Object[]> doanhThuTheoNgay(@Param("tuNgay") LocalDateTime tuNgay,
                                    @Param("denNgay") LocalDateTime denNgay);

    /**
     * Doanh thu group theo tháng (cho biểu đồ).
     * Trả về List<Object[]> = [thang (int), nam (int), tongTien]
     */
    @Query(value = "SELECT MONTH(d.ngay_tao) AS thang, YEAR(d.ngay_tao) AS nam, COALESCE(SUM(d.tong_tien), 0) AS tong " +
            "FROM don_hang d " +
            "WHERE d.ngay_tao >= :tuNgay AND d.ngay_tao <= :denNgay " +
            "GROUP BY YEAR(d.ngay_tao), MONTH(d.ngay_tao) " +
            "ORDER BY nam, thang",
            nativeQuery = true)
    List<Object[]> doanhThuTheoThang(@Param("tuNgay") LocalDateTime tuNgay,
                                     @Param("denNgay") LocalDateTime denNgay);

    // ==================== TOP SẢN PHẨM BÁN CHẠY ====================

    /**
     * Top sản phẩm bán chạy nhất (theo tổng số lượng bán).
     * Trả về List<Object[]> = [tenSanPham (String), tongSoLuong (Long)]
     *
     * JOIN: don_hang_chi_tiet → san_pham_chi_tiet → san_pham
     */
    @Query(value = "SELECT TOP 5 sp.ten_san_pham, SUM(ct.so_luong) AS tong_sl " +
            "FROM don_hang_chi_tiet ct " +
            "JOIN san_pham_chi_tiet spct ON ct.san_pham_chi_tiet_id = spct.id " +
            "JOIN san_pham sp ON spct.san_pham_id = sp.id " +
            "JOIN don_hang dh ON ct.don_hang_id = dh.id " +
            "WHERE dh.ngay_tao >= :tuNgay AND dh.ngay_tao <= :denNgay " +
            "GROUP BY sp.ten_san_pham " +
            "ORDER BY tong_sl DESC",
            nativeQuery = true)
    List<Object[]> topSanPhamBanChay(@Param("tuNgay") LocalDateTime tuNgay,
                                     @Param("denNgay") LocalDateTime denNgay);

    /**
     * Top sản phẩm bán chạy (không giới hạn ngày).
     */
    @Query(value = "SELECT TOP 5 sp.ten_san_pham, SUM(ct.so_luong) AS tong_sl " +
            "FROM don_hang_chi_tiet ct " +
            "JOIN san_pham_chi_tiet spct ON ct.san_pham_chi_tiet_id = spct.id " +
            "JOIN san_pham sp ON spct.san_pham_id = sp.id " +
            "GROUP BY sp.ten_san_pham " +
            "ORDER BY tong_sl DESC",
            nativeQuery = true)
    List<Object[]> topSanPhamBanChayAll();
}
