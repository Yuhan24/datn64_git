package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.KhachHang;
import com.poly.shopquanao.repository.admin.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller quản lý khách hàng (Admin).
 *
 * Chức năng:
 *   - Xem danh sách khách hàng (lọc theo trạng thái: hoạt động / bị khoá)
 *   - Thêm khách hàng mới (form ẩn hiện, mã hoá BCrypt)
 *   - Sửa thông tin khách hàng
 *   - Khoá / mở khoá tài khoản khách hàng
 *
 * URL gốc: /admin/customer
 */
@Controller
@RequestMapping("/admin/customer")
public class KhachHangController {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ======================== DANH SÁCH + LỌC TRẠNG THÁI ========================

    /**
     * GET /admin/customer?trangThai=true (mặc định)
     * GET /admin/customer?trangThai=false (xem KH bị khoá)
     */
    @GetMapping("")
    public String index(@RequestParam(defaultValue = "true") Boolean trangThai,
                        Model model) {

        model.addAttribute("pageTitle", "Quản lý khách hàng");
        model.addAttribute("activeMenu", "customer");
        model.addAttribute("content", "admin/customer :: content");

        List<KhachHang> danhSach = khachHangRepository.findByTrangThai(trangThai);
        model.addAttribute("listKhachHang", danhSach);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("khachHang", new KhachHang());

        return "admin/layout";
    }

    // ======================== THÊM MỚI ========================

    @PostMapping("/save")
    public String save(@RequestParam(required = false) String maKhachHang,
                       @RequestParam String hoTen,
                       @RequestParam String tenDangNhap,
                       @RequestParam String matKhau,
                       @RequestParam(required = false) String soDienThoai,
                       @RequestParam(required = false) String diaChi,
                       RedirectAttributes ra) {

        String username = tenDangNhap == null ? "" : tenDangNhap.trim();
        if (username.isEmpty()) {
            ra.addFlashAttribute("error", "Tên đăng nhập không được để trống.");
            return "redirect:/admin/customer";
        }

        if (khachHangRepository.existsByTenDangNhap(username)) {
            ra.addFlashAttribute("error", "Tên đăng nhập \"" + username + "\" đã tồn tại!");
            return "redirect:/admin/customer";
        }

        if (matKhau == null || matKhau.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Mật khẩu không được để trống.");
            return "redirect:/admin/customer";
        }

        KhachHang kh = new KhachHang();
        kh.setMaKhachHang(maKhachHang != null && !maKhachHang.trim().isEmpty()
                ? maKhachHang.trim() : "KH" + System.currentTimeMillis());
        kh.setHoTen(hoTen);
        kh.setTenDangNhap(username);
        kh.setMatKhau(passwordEncoder.encode(matKhau));
        kh.setSoDienThoai(soDienThoai);
        kh.setDiaChi(diaChi);
        kh.setTrangThai(true);

        khachHangRepository.save(kh);
        ra.addFlashAttribute("success", "Thêm khách hàng \"" + hoTen + "\" thành công!");
        return "redirect:/admin/customer";
    }

    // ======================== SỬA – LOAD FORM ========================

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id,
                           @RequestParam(defaultValue = "true") Boolean trangThai,
                           Model model) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng id=" + id));

        model.addAttribute("pageTitle", "Sửa khách hàng");
        model.addAttribute("activeMenu", "customer");
        model.addAttribute("content", "admin/customer :: content");

        model.addAttribute("listKhachHang", khachHangRepository.findByTrangThai(trangThai));
        model.addAttribute("khachHang", kh);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("isEdit", true);
        model.addAttribute("showForm", true);

        return "admin/layout";
    }

    // ======================== SỬA – XỬ LÝ ========================

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id,
                         @RequestParam(required = false) String maKhachHang,
                         @RequestParam String hoTen,
                         @RequestParam String tenDangNhap,
                         @RequestParam(required = false) String matKhau,
                         @RequestParam(required = false) String soDienThoai,
                         @RequestParam(required = false) String diaChi,
                         RedirectAttributes ra) {

        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy KH id=" + id));

        String username = tenDangNhap == null ? "" : tenDangNhap.trim();
        if (!kh.getTenDangNhap().equals(username) && khachHangRepository.existsByTenDangNhap(username)) {
            ra.addFlashAttribute("error", "Tên đăng nhập \"" + username + "\" đã tồn tại!");
            return "redirect:/admin/customer/edit/" + id;
        }

        if (maKhachHang != null && !maKhachHang.trim().isEmpty()) {
            kh.setMaKhachHang(maKhachHang.trim());
        }
        kh.setHoTen(hoTen);
        kh.setTenDangNhap(username);
        kh.setSoDienThoai(soDienThoai);
        kh.setDiaChi(diaChi);

        if (matKhau != null && !matKhau.trim().isEmpty()) {
            kh.setMatKhau(passwordEncoder.encode(matKhau));
        }

        khachHangRepository.save(kh);
        ra.addFlashAttribute("success", "Cập nhật khách hàng \"" + hoTen + "\" thành công!");
        return "redirect:/admin/customer";
    }

    // ======================== KHOÁ / MỞ KHOÁ ========================

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Integer id,
                               @RequestParam(defaultValue = "true") Boolean currentFilter,
                               RedirectAttributes ra) {
        KhachHang kh = khachHangRepository.findById(id).orElseThrow();
        boolean newStatus = !Boolean.TRUE.equals(kh.getTrangThai());
        kh.setTrangThai(newStatus);
        khachHangRepository.save(kh);

        String msg = newStatus ? "Đã mở khoá khách hàng \"" + kh.getHoTen() + "\""
                : "Đã khoá khách hàng \"" + kh.getHoTen() + "\"";
        ra.addFlashAttribute("success", msg);

        return "redirect:/admin/customer?trangThai=" + currentFilter;
    }
}