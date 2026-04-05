package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.DonHang;
import com.poly.shopquanao.repository.admin.DonHangADRepository;
import com.poly.shopquanao.repository.admin.KhachHangRepository;
import com.poly.shopquanao.repository.admin.SanPhamADMRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller trang Thống kê chi tiết (Admin).
 *
 * Chức năng:
 *   - Trang thống kê với bộ lọc: theo ngày, theo tháng, theo quý, theo năm
 *   - REST API trả JSON cho biểu đồ Chart.js (doanh thu theo ngày/tháng, top SP)
 *   - Thông kê tổng quan có lọc khoảng ngày
 *
 * URL gốc: /admin/thong-ke
 */
@Controller
@RequestMapping("/admin/thong-ke")
public class ThongKeController {

    @Autowired
    private DonHangADRepository donHangRepo;

    @Autowired
    private KhachHangRepository khachHangRepo;

    @Autowired
    private SanPhamADMRepository sanPhamRepo;

    // ==================== TRANG THỐNG KÊ CHÍNH ====================

    /**
     * GET /admin/thong-ke
     * Hiển thị trang thống kê với bộ lọc ngày.
     *
     * Tham số tuỳ chọn:
     *   - tuNgay:  ngày bắt đầu (yyyy-MM-dd), mặc định = đầu tháng hiện tại
     *   - denNgay: ngày kết thúc (yyyy-MM-dd), mặc định = hôm nay
     *   - loai:    loại thống kê ("ngay" / "thang" / "quy" / "nam"), mặc định = "ngay"
     */
    @GetMapping("")
    public String thongKe(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false, defaultValue = "ngay") String loai,
            Model model) {

        // --- Giá trị mặc định nếu không truyền ---
        if (tuNgay == null) tuNgay = LocalDate.now().withDayOfMonth(1);   // đầu tháng
        if (denNgay == null) denNgay = LocalDate.now();                   // hôm nay

        // Chuyển LocalDate → LocalDateTime (bắt đầu 00:00, kết thúc 23:59:59)
        LocalDateTime tuDateTime = tuNgay.atStartOfDay();
        LocalDateTime denDateTime = denNgay.atTime(LocalTime.MAX);

        // ==================== THUỘC TÍNH SIDEBAR ====================
        model.addAttribute("pageTitle", "Thống kê");
        model.addAttribute("activeMenu", "thongke");
        model.addAttribute("content", "admin/thong-ke :: content");

        // ==================== GIỮ LẠI GIÁ TRỊ BỘ LỌC ====================
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        model.addAttribute("loai", loai);

        // ==================== THỐNG KÊ TỔNG QUAN (TRONG KHOẢNG NGÀY) ====================

        // Tổng đơn hàng trong khoảng
        long tongDon = donHangRepo.countByDateRange(tuDateTime, denDateTime);
        model.addAttribute("tongDon", tongDon);

        // Tổng doanh thu trong khoảng
        BigDecimal tongDoanhThu = donHangRepo.sumDoanhThuByDateRange(tuDateTime, denDateTime);
        model.addAttribute("tongDoanhThu", tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO);

        // Doanh thu hoàn thành trong khoảng
        BigDecimal doanhThuHT = donHangRepo.sumDoanhThuHoanThanhByDateRange(tuDateTime, denDateTime);
        model.addAttribute("doanhThuHoanThanh", doanhThuHT != null ? doanhThuHT : BigDecimal.ZERO);

        // Đơn theo trạng thái trong khoảng
        model.addAttribute("donChoXacNhan", donHangRepo.countByTrangThaiIdAndDateRange(1, tuDateTime, denDateTime));
        model.addAttribute("donDangGiao", donHangRepo.countByTrangThaiIdAndDateRange(2, tuDateTime, denDateTime));
        model.addAttribute("donHoanThanh", donHangRepo.countByTrangThaiIdAndDateRange(3, tuDateTime, denDateTime));
        model.addAttribute("donDaHuy", donHangRepo.countByTrangThaiIdAndDateRange(4, tuDateTime, denDateTime));

        // ==================== DỮ LIỆU BIỂU ĐỒ ====================

        // 1. Doanh thu theo ngày (cho biểu đồ cột)
        List<Object[]> doanhThuNgay = donHangRepo.doanhThuTheoNgay(tuDateTime, denDateTime);
        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartValues = new ArrayList<>();
        for (Object[] row : doanhThuNgay) {
            chartLabels.add(row[0].toString());                                  // ngày (yyyy-MM-dd)
            // ⭐ SQL Server có thể trả Integer hoặc BigDecimal → cast an toàn qua Number
            chartValues.add(new BigDecimal(((Number) row[1]).toString()));        // tổng tiền
        }
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);

        // 2. Doanh thu theo tháng (cho biểu đồ đường)
        //    Query trả về: [0]=thang (int), [1]=nam (int), [2]=tongTien (BigDecimal)
        List<Object[]> doanhThuThang = donHangRepo.doanhThuTheoThang(tuDateTime, denDateTime);
        List<String> monthLabels = new ArrayList<>();
        List<BigDecimal> monthValues = new ArrayList<>();
        for (Object[] row : doanhThuThang) {
            monthLabels.add("T" + row[0] + "/" + row[1]);                        // VD: "T3/2026"
            // ⭐ FIX: dùng row[2] (tongTien) thay vì row[1] (nam)
            monthValues.add(new BigDecimal(((Number) row[2]).toString()));        // tổng tiền
        }
        model.addAttribute("monthLabels", monthLabels);
        model.addAttribute("monthValues", monthValues);

        // 3. Top sản phẩm bán chạy (cho biểu đồ ngang)
        List<Object[]> topSP = donHangRepo.topSanPhamBanChay(tuDateTime, denDateTime);
        List<String> topLabels = new ArrayList<>();
        List<Long> topValues = new ArrayList<>();
        for (Object[] row : topSP) {
            topLabels.add((String) row[0]);               // tên sản phẩm
            topValues.add(((Number) row[1]).longValue());  // tổng số lượng
        }
        model.addAttribute("topLabels", topLabels);
        model.addAttribute("topValues", topValues);

        // 4. Đơn hàng trong khoảng ngày (bảng chi tiết)
        List<DonHang> donHangList = donHangRepo.findByDateRange(tuDateTime, denDateTime);
        model.addAttribute("donHangList", donHangList);

        return "admin/layout";
    }

    // ==================== BỘ LỌC NHANH: THEO QUÝ ====================

    /**
     * GET /admin/thong-ke/quy?quy=1&nam=2026
     * Redirect sang trang thống kê với ngày bắt đầu/kết thúc của quý đã chọn.
     */
    @GetMapping("/quy")
    public String thongKeTheoQuy(@RequestParam int quy, @RequestParam int nam) {
        LocalDate tuNgay;
        LocalDate denNgay;

        // Tính ngày đầu và cuối của quý
        switch (quy) {
            case 1:
                tuNgay = LocalDate.of(nam, 1, 1);
                denNgay = LocalDate.of(nam, 3, 31);
                break;
            case 2:
                tuNgay = LocalDate.of(nam, 4, 1);
                denNgay = LocalDate.of(nam, 6, 30);
                break;
            case 3:
                tuNgay = LocalDate.of(nam, 7, 1);
                denNgay = LocalDate.of(nam, 9, 30);
                break;
            case 4:
                tuNgay = LocalDate.of(nam, 10, 1);
                denNgay = LocalDate.of(nam, 12, 31);
                break;
            default:
                tuNgay = LocalDate.of(nam, 1, 1);
                denNgay = LocalDate.of(nam, 12, 31);
        }

        return "redirect:/admin/thong-ke?tuNgay=" + tuNgay + "&denNgay=" + denNgay + "&loai=thang";
    }

    // ==================== BỘ LỌC NHANH: THEO NĂM ====================

    /**
     * GET /admin/thong-ke/nam?nam=2026
     * Thống kê cả năm.
     */
    @GetMapping("/nam")
    public String thongKeTheoNam(@RequestParam int nam) {
        LocalDate tuNgay = LocalDate.of(nam, 1, 1);
        LocalDate denNgay = LocalDate.of(nam, 12, 31);
        return "redirect:/admin/thong-ke?tuNgay=" + tuNgay + "&denNgay=" + denNgay + "&loai=thang";
    }

    // ==================== BỘ LỌC NHANH: HÔM NAY ====================

    @GetMapping("/hom-nay")
    public String thongKeHomNay() {
        LocalDate today = LocalDate.now();
        return "redirect:/admin/thong-ke?tuNgay=" + today + "&denNgay=" + today;
    }

    // ==================== BỘ LỌC NHANH: 7 NGÀY GẦN NHẤT ====================

    @GetMapping("/7-ngay")
    public String thongKe7Ngay() {
        LocalDate denNgay = LocalDate.now();
        LocalDate tuNgay = denNgay.minusDays(6);
        return "redirect:/admin/thong-ke?tuNgay=" + tuNgay + "&denNgay=" + denNgay;
    }

    // ==================== BỘ LỌC NHANH: THÁNG NÀY ====================

    @GetMapping("/thang-nay")
    public String thongKeThangNay() {
        LocalDate today = LocalDate.now();
        LocalDate tuNgay = today.withDayOfMonth(1);
        LocalDate denNgay = today.with(TemporalAdjusters.lastDayOfMonth());
        return "redirect:/admin/thong-ke?tuNgay=" + tuNgay + "&denNgay=" + denNgay;
    }
}