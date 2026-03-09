package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.NhanVien;
import com.poly.shopquanao.repository.VaiTroRepository;
import com.poly.shopquanao.repository.admin.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller quản lý nhân viên (Admin).
 *
 * Chức năng:
 *   - Xem danh sách nhân viên (lọc theo trạng thái: hoạt động / nghỉ việc)
 *   - Thêm nhân viên mới (form ẩn hiện, mã hoá BCrypt)
 *   - Sửa thông tin nhân viên
 *   - Bật / tắt trạng thái (khôi phục nhân viên nghỉ việc)
 *
 * URL gốc: /admin/employee
 */
@Controller
@RequestMapping("/admin/employee")
public class NhanVienController {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ======================== DANH SÁCH + LỌC TRẠNG THÁI ========================

    /**
     * GET /admin/employee?trangThai=true (mặc định)
     * GET /admin/employee?trangThai=false (xem nhân viên nghỉ việc)
     *
     * @param trangThai  true = hoạt động (mặc định), false = nghỉ việc
     */
    @GetMapping("")
    public String index(@RequestParam(defaultValue = "true") Boolean trangThai,
                        Model model) {

        // Sidebar
        model.addAttribute("pageTitle", "Quản lý nhân viên");
        model.addAttribute("activeMenu", "employee");
        model.addAttribute("content", "admin/employee :: content");

        // Lọc theo trạng thái
        List<NhanVien> danhSach = nhanVienRepository.findByTrangThai(trangThai);
        model.addAttribute("listNhanVien", danhSach);
        model.addAttribute("trangThai", trangThai);           // giữ lại giá trị combobox
        model.addAttribute("listVaiTro", vaiTroRepository.findAll());
        model.addAttribute("nhanVien", new NhanVien());       // form thêm mới

        return "admin/layout";
    }

    // ======================== THÊM MỚI ========================

    /**
     * POST /admin/employee/save
     * Validate → mã hoá BCrypt → lưu → redirect về danh sách + toast thành công.
     */
    @PostMapping("/save")
    public String save(@RequestParam String maNhanVien,
                       @RequestParam String hoTen,
                       @RequestParam String tenDangNhap,
                       @RequestParam String matKhau,
                       @RequestParam(required = false) String soDienThoai,
                       @RequestParam Integer vaiTroId,
                       RedirectAttributes ra) {

        // Validate tên đăng nhập
        String username = tenDangNhap == null ? "" : tenDangNhap.trim();
        if (username.isEmpty()) {
            ra.addFlashAttribute("error", "Tên đăng nhập không được để trống.");
            return "redirect:/admin/employee";
        }

        // Kiểm tra trùng tên đăng nhập
        if (nhanVienRepository.existsByTenDangNhap(username)) {
            ra.addFlashAttribute("error", "Tên đăng nhập \"" + username + "\" đã tồn tại!");
            return "redirect:/admin/employee";
        }

        // Validate mật khẩu
        if (matKhau == null || matKhau.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Mật khẩu không được để trống.");
            return "redirect:/admin/employee";
        }

        // Tạo nhân viên mới
        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(maNhanVien != null && !maNhanVien.trim().isEmpty()
                ? maNhanVien.trim() : "NV" + System.currentTimeMillis());
        nv.setHoTen(hoTen);
        nv.setTenDangNhap(username);
        nv.setMatKhau(passwordEncoder.encode(matKhau));   // ⭐ BCrypt
        nv.setSoDienThoai(soDienThoai);
        nv.setVaiTro(vaiTroRepository.findById(vaiTroId).orElseThrow());
        nv.setTrangThai(true);

        nhanVienRepository.save(nv);
        ra.addFlashAttribute("success", "Thêm nhân viên \"" + hoTen + "\" thành công!");
        return "redirect:/admin/employee";
    }

    // ======================== SỬA – LOAD FORM ========================

    /**
     * GET /admin/employee/edit/{id}
     * Load thông tin NV vào form sửa (form hiện ra trên trang).
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id,
                           @RequestParam(defaultValue = "true") Boolean trangThai,
                           Model model) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên id=" + id));

        model.addAttribute("pageTitle", "Sửa nhân viên");
        model.addAttribute("activeMenu", "employee");
        model.addAttribute("content", "admin/employee :: content");

        model.addAttribute("listNhanVien", nhanVienRepository.findByTrangThai(trangThai));
        model.addAttribute("nhanVien", nv);
        model.addAttribute("listVaiTro", vaiTroRepository.findAll());
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("isEdit", true);       // ⭐ Flag để template mở form sửa
        model.addAttribute("showForm", true);     // ⭐ Tự mở form

        return "admin/layout";
    }

    // ======================== SỬA – XỬ LÝ ========================

    /**
     * POST /admin/employee/update/{id}
     * Nếu mật khẩu trống → giữ nguyên mật khẩu cũ.
     */
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id,
                         @RequestParam String maNhanVien,
                         @RequestParam String hoTen,
                         @RequestParam String tenDangNhap,
                         @RequestParam(required = false) String matKhau,
                         @RequestParam(required = false) String soDienThoai,
                         @RequestParam Integer vaiTroId,
                         RedirectAttributes ra) {

        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên id=" + id));

        // Kiểm tra trùng tên đăng nhập (nếu đổi)
        String username = tenDangNhap == null ? "" : tenDangNhap.trim();
        if (!nv.getTenDangNhap().equals(username) && nhanVienRepository.existsByTenDangNhap(username)) {
            ra.addFlashAttribute("error", "Tên đăng nhập \"" + username + "\" đã tồn tại!");
            return "redirect:/admin/employee/edit/" + id;
        }

        // Cập nhật thông tin
        nv.setMaNhanVien(maNhanVien != null ? maNhanVien.trim() : nv.getMaNhanVien());
        nv.setHoTen(hoTen);
        nv.setTenDangNhap(username);
        nv.setSoDienThoai(soDienThoai);
        nv.setVaiTro(vaiTroRepository.findById(vaiTroId).orElseThrow());

        // Mật khẩu: chỉ đổi nếu có nhập mới
        if (matKhau != null && !matKhau.trim().isEmpty()) {
            nv.setMatKhau(passwordEncoder.encode(matKhau));
        }

        nhanVienRepository.save(nv);
        ra.addFlashAttribute("success", "Cập nhật nhân viên \"" + hoTen + "\" thành công!");
        return "redirect:/admin/employee";
    }

    // ======================== BẬT / TẮT TRẠNG THÁI ========================

    /**
     * POST /admin/employee/{id}/toggle-status
     * Đổi trạng thái: hoạt động ↔ nghỉ việc.
     * Redirect giữ nguyên filter trạng thái hiện tại.
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Integer id,
                               @RequestParam(defaultValue = "true") Boolean currentFilter,
                               RedirectAttributes ra) {
        NhanVien nv = nhanVienRepository.findById(id).orElseThrow();
        boolean newStatus = !Boolean.TRUE.equals(nv.getTrangThai());
        nv.setTrangThai(newStatus);
        nhanVienRepository.save(nv);

        // Thông báo phù hợp
        String msg = newStatus ? "Đã khôi phục nhân viên \"" + nv.getHoTen() + "\""
                : "Đã cho nhân viên \"" + nv.getHoTen() + "\" nghỉ việc";
        ra.addFlashAttribute("success", msg);

        return "redirect:/admin/employee?trangThai=" + currentFilter;
    }
}
