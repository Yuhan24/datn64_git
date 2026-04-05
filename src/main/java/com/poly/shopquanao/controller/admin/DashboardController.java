package com.poly.shopquanao.controller.admin;


import com.poly.shopquanao.entity.DonHang;
import com.poly.shopquanao.entity.SanPhamChiTiet;
import com.poly.shopquanao.repository.admin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/admin/dashboard")
public class DashboardController {


    @Autowired
    private SanPhamADMRepository sanPhamRepo;


    @Autowired
    private KhachHangRepository khachHangRepo;


    @Autowired
    private DonHangADRepository donHangRepo;


    @Autowired
    private NhanVienRepository nhanVienRepo;


    @Autowired
    private SanPhamChiTietADMRepository sanPhamChiTietRepo;




    @Value("${app.lowstock.threshold:10}")
    private int lowStockThreshold;


    @GetMapping("")
    public String dashboard(Model model) {


        // Sidebar
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("content", "admin/dashboard :: content");


        // ===== CARD THỐNG KÊ =====
        model.addAttribute("tongSanPham", sanPhamRepo.count());
        model.addAttribute("tongKhachHang", khachHangRepo.count());
        model.addAttribute("tongNhanVien", nhanVienRepo.count());
        model.addAttribute("tongDonHang", donHangRepo.count());


        // Tổng doanh thu
        BigDecimal tongDoanhThu = donHangRepo.sumTongTien();
        model.addAttribute("tongDoanhThu", tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO);


        // Doanh thu đơn hoàn thành
        BigDecimal doanhThuHT = donHangRepo.sumDoanhThuHoanThanh();
        model.addAttribute("doanhThuHoanThanh", doanhThuHT != null ? doanhThuHT : BigDecimal.ZERO);


        // ===== THỐNG KÊ TRẠNG THÁI ĐƠN =====
        long donChoXacNhan = donHangRepo.countByTrangThaiId(1);
        long donDangGiao = donHangRepo.countByTrangThaiId(2);
        long donHoanThanh = donHangRepo.countByTrangThaiId(3);
        long donDaHuy = donHangRepo.countByTrangThaiId(4);


        model.addAttribute("donChoXacNhan", donChoXacNhan);
        model.addAttribute("donDangGiao", donDangGiao);
        model.addAttribute("donHoanThanh", donHoanThanh);
        model.addAttribute("donDaHuy", donDaHuy);


        // ===== TOP SẢN PHẨM BÁN CHẠY =====
        List<Object[]> topSP = donHangRepo.topSanPhamBanChayAll();
        List<String> topSPLabels = new ArrayList<>();
        List<Long> topSPValues = new ArrayList<>();


        for (Object[] row : topSP) {
            topSPLabels.add((String) row[0]);
            topSPValues.add(((Number) row[1]).longValue());
        }


        model.addAttribute("topSPLabels", topSPLabels);
        model.addAttribute("topSPValues", topSPValues);


        // ===== 5 ĐƠN HÀNG GẦN NHẤT =====
        List<DonHang> donHangGanNhat = donHangRepo.findTop5ByOrderByNgayTaoDesc();
        model.addAttribute("donHangGanNhat", donHangGanNhat);




        return "admin/layout";
    }
}



