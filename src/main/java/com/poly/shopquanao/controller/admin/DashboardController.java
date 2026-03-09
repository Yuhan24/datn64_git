package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.DonHang;
import com.poly.shopquanao.repository.admin.DonHangRepository;
import com.poly.shopquanao.repository.admin.KhachHangRepository;
import com.poly.shopquanao.repository.admin.NhanVienRepository;
import com.poly.shopquanao.repository.admin.SanPhamADMRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller Dashboard (Admin) – Trang thống kê tổng quan.
 *
 * Hiển thị:
 *   - 4 card thống kê: Tổng sản phẩm, Tổng khách hàng, Tổng đơn hàng, Doanh thu
 *   - 4 card trạng thái đơn hàng
 *   - 2 biểu đồ Chart.js: Trạng thái đơn (Doughnut) + Top SP bán chạy (Bar ngang)
 *   - Bảng 5 đơn hàng gần nhất
 *   - Nút link sang trang Thống kê chi tiết
 */
@Controller
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private SanPhamADMRepository sanPhamRepo;

    @Autowired
    private KhachHangRepository khachHangRepo;

    @Autowired
    private DonHangRepository donHangRepo;

    @Autowired
    private NhanVienRepository nhanVienRepo;

    /**
     * GET /admin/dashboard
     * Truy vấn dữ liệu thống kê và truyền vào model cho Thymeleaf + Chart.js render.
     */
    @GetMapping("")
    public String dashboard(Model model) {

        // ==================== THUỘC TÍNH SIDEBAR ====================
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("content", "admin/dashboard :: content");

        // ==================== CARD THỐNG KÊ ====================

        // Tổng sản phẩm
        model.addAttribute("tongSanPham", sanPhamRepo.count());

        // Tổng khách hàng
        model.addAttribute("tongKhachHang", khachHangRepo.count());

        // Tổng nhân viên
        model.addAttribute("tongNhanVien", nhanVienRepo.count());

        // Tổng đơn hàng
        model.addAttribute("tongDonHang", donHangRepo.count());

        // Tổng doanh thu (tất cả đơn)
        BigDecimal tongDoanhThu = donHangRepo.sumTongTien();
        model.addAttribute("tongDoanhThu", tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO);

        // Doanh thu đơn hoàn thành
        BigDecimal doanhThuHT = donHangRepo.sumDoanhThuHoanThanh();
        model.addAttribute("doanhThuHoanThanh", doanhThuHT != null ? doanhThuHT : BigDecimal.ZERO);

        // ==================== THỐNG KÊ THEO TRẠNG THÁI ====================

        long donChoXacNhan = donHangRepo.countByTrangThaiId(1);
        long donDangGiao = donHangRepo.countByTrangThaiId(2);
        long donHoanThanh = donHangRepo.countByTrangThaiId(3);
        long donDaHuy = donHangRepo.countByTrangThaiId(4);

        model.addAttribute("donChoXacNhan", donChoXacNhan);
        model.addAttribute("donDangGiao", donDangGiao);
        model.addAttribute("donHoanThanh", donHoanThanh);
        model.addAttribute("donDaHuy", donDaHuy);

        // ==================== TOP SẢN PHẨM BÁN CHẠY (cho biểu đồ) ====================

        List<Object[]> topSP = donHangRepo.topSanPhamBanChayAll();
        List<String> topSPLabels = new ArrayList<>();
        List<Long> topSPValues = new ArrayList<>();
        for (Object[] row : topSP) {
            topSPLabels.add((String) row[0]);               // tên sản phẩm
            topSPValues.add(((Number) row[1]).longValue());  // tổng số lượng
        }
        model.addAttribute("topSPLabels", topSPLabels);
        model.addAttribute("topSPValues", topSPValues);

        // ==================== 5 ĐƠN HÀNG GẦN NHẤT ====================

        List<DonHang> donHangGanNhat = donHangRepo.findTop5ByOrderByNgayTaoDesc();
        if (donHangGanNhat.size() > 5) {
            donHangGanNhat = donHangGanNhat.subList(0, 5);
        }
        model.addAttribute("donHangGanNhat", donHangGanNhat);

        return "admin/layout";
    }
}
