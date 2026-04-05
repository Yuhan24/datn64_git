package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.DonHang;
import com.poly.shopquanao.entity.DonHangChiTiet;
import com.poly.shopquanao.entity.SanPhamChiTiet;
import com.poly.shopquanao.repository.admin.DonHangADRepository;
import com.poly.shopquanao.repository.admin.DonHangChiTietADRepository;
import com.poly.shopquanao.repository.admin.NhanVienRepository;
import com.poly.shopquanao.repository.admin.SanPhamChiTietADMRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/order-onl")
public class OrderOnlineController {

    private static final int CHO_XAC_NHAN = 1;
    private static final int DANG_GIAO = 2;
    private static final int HOAN_THANH = 3;
    private static final int DA_HUY = 4;

    @Autowired
    private DonHangADRepository donHangADRepository;

    @Autowired
    private DonHangChiTietADRepository donHangChiTietADRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private SanPhamChiTietADMRepository sanPhamChiTietADMRepository;

    @GetMapping("")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<DonHang> pageData = donHangADRepository
                .findByLoaiDonOrderByNgayTaoDesc(0, PageRequest.of(page, size));

        model.addAttribute("pageTitle", "Hóa đơn online");
        model.addAttribute("activeGroup", "order");
        model.addAttribute("activeMenu", "order-onl");
        model.addAttribute("content", "admin/order-onl :: content");

        model.addAttribute("listOrder", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("size", size);

        return "admin/layout";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        DonHang donHang = donHangADRepository.findById(id).orElseThrow();
        List<DonHangChiTiet> chiTietList = donHangChiTietADRepository.findByDonHang_Id(id);

        model.addAttribute("pageTitle", "Chi tiết hóa đơn online");
        model.addAttribute("activeGroup", "order");
        model.addAttribute("activeMenu", "order-onl");
        model.addAttribute("content", "admin/order-onl-detail :: content");

        model.addAttribute("donHang", donHang);
        model.addAttribute("chiTietList", chiTietList);

        return "admin/layout";
    }

    @PostMapping("/{id}/confirm")
    @Transactional
    public String confirm(@PathVariable Integer id,
                          RedirectAttributes ra,
                          Authentication authentication) {
        DonHang donHang = donHangADRepository.findById(id).orElseThrow();

        if (donHang.getTrangThaiId() != 1) {
            ra.addFlashAttribute("error", "Chỉ đơn chờ xác nhận mới được xác nhận");
            return "redirect:/admin/order-onl/detail/" + id;
        }

        String pttt = donHang.getPhuongThucThanhToan();
        String ttThanhToan = donHang.getTrangThaiThanhToan();

        boolean laChuyenKhoan = "CHUYEN_KHOAN".equalsIgnoreCase(pttt);
        boolean daThanhToan = "DA_THANH_TOAN".equalsIgnoreCase(ttThanhToan);

        if (laChuyenKhoan && !daThanhToan) {
            ra.addFlashAttribute("error", "Đơn chuyển khoản chưa thanh toán, không thể xác nhận");
            return "redirect:/admin/order-onl/detail/" + id;
        }

        List<DonHangChiTiet> chiTietList = donHangChiTietADRepository.findByDonHang_Id(id);

        for (DonHangChiTiet ct : chiTietList) {
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();

            if (spct.getSoLuong() < ct.getSoLuong()) {
                ra.addFlashAttribute("error",
                        "Sản phẩm " + spct.getSanPham().getTenSanPham() + " không đủ tồn kho");
                return "redirect:/admin/order-onl/detail/" + id;
            }
        }

        boolean daThanhToan1 = "DA_THANH_TOAN".equalsIgnoreCase(donHang.getTrangThaiThanhToan());

// COD → trừ kho khi confirm
        if (!daThanhToan1) {

            for (DonHangChiTiet ct : chiTietList) {
                SanPhamChiTiet spct = ct.getSanPhamChiTiet();

                if (spct.getSoLuong() < ct.getSoLuong()) {
                    ra.addFlashAttribute("error",
                            "Sản phẩm " + spct.getSanPham().getTenSanPham() + " không đủ tồn kho");
                    return "redirect:/admin/order-onl/detail/" + id;
                }
            }

            for (DonHangChiTiet ct : chiTietList) {
                SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                spct.setSoLuong(spct.getSoLuong() - ct.getSoLuong());
                sanPhamChiTietADMRepository.save(spct);
            }

        } else {
            // Online → chỉ xác nhận, không đụng kho
            ra.addFlashAttribute("success", "Đơn đã thanh toán - không trừ kho");
        }

        if (authentication != null) {
            String username = authentication.getName();
            nhanVienRepository.findByTenDangNhap(username).ifPresent(donHang::setNhanVien);
        }

        donHang.setTrangThaiId(2);
        donHangADRepository.save(donHang);

        ra.addFlashAttribute("success", "Đơn hàng đã được xác nhận");
        return "redirect:/admin/order-onl/detail/" + id;
    }




    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Integer id,
                           RedirectAttributes ra,
                           Authentication authentication) {
        DonHang donHang = donHangADRepository.findById(id).orElseThrow();

        if (donHang.getTrangThaiId() != 2) {
            ra.addFlashAttribute("error", "Chỉ đơn đang giao mới được hoàn thành");
            return "redirect:/admin/order-onl/detail/" + id;
        }

        String pttt = donHang.getPhuongThucThanhToan();
        String ttThanhToan = donHang.getTrangThaiThanhToan();

        boolean laCOD = "COD".equalsIgnoreCase(pttt);
        boolean laChuyenKhoan = "CHUYEN_KHOAN".equalsIgnoreCase(pttt);
        boolean daThanhToan = "DA_THANH_TOAN".equalsIgnoreCase(ttThanhToan);

        // Chuyển khoản mà chưa thanh toán thì không cho hoàn thành
        if (laChuyenKhoan && !daThanhToan) {
            ra.addFlashAttribute("error", "Đơn chuyển khoản chưa thanh toán, không thể hoàn thành");
            return "redirect:/admin/order-onl/detail/" + id;
        }

        if (authentication != null) {
            String username = authentication.getName();
            nhanVienRepository.findByTenDangNhap(username)
                    .ifPresent(donHang::setNhanVien);
        }

        donHang.setTrangThaiId(3);

        // COD thì lúc hoàn thành mới coi là đã thanh toán
        if (laCOD) {
            donHang.setTrangThaiThanhToan("DA_THANH_TOAN");
        }

        donHangADRepository.save(donHang);

        ra.addFlashAttribute("success", "Đơn hàng đã hoàn thành");
        return "redirect:/admin/order-onl/detail/" + id;
    }

    @PostMapping("/{id}/cancel")
    @Transactional
    public String cancel(@PathVariable Integer id, RedirectAttributes ra) {
        DonHang donHang = donHangADRepository.findById(id).orElseThrow();

        if (donHang.getTrangThaiId() == 4) {
            ra.addFlashAttribute("error", "Đơn hàng đã hủy trước đó");
            return "redirect:/admin/order-onl/detail/" + id;
        }

        if (donHang.getTrangThaiId() == 3) {
            ra.addFlashAttribute("error", "Đơn hoàn thành không thể hủy");
            return "redirect:/admin/order-onl/detail/" + id;
        }

        if (donHang.getTrangThaiId() != 1) {
            ra.addFlashAttribute("error", "Chỉ được hủy đơn khi đang chờ xác nhận");
            return "redirect:/admin/order-onl/detail/" + id;
        }

        if ("DA_THANH_TOAN".equalsIgnoreCase(donHang.getTrangThaiThanhToan())) {
            ra.addFlashAttribute("error", "Đơn đã thanh toán, không thể hủy");
            return "redirect:/admin/order-onl/detail/" + id;
        }

        donHang.setTrangThaiId(4);
        donHangADRepository.save(donHang);

        ra.addFlashAttribute("success", "Đã hủy đơn hàng");
        return "redirect:/admin/order-onl/detail/" + id;
    }
}