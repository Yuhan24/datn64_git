package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.SanPham;
import com.poly.shopquanao.repository.admin.DanhMucRepository;
import com.poly.shopquanao.repository.admin.SanPhamADMRepository;
import com.poly.shopquanao.repository.admin.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/product")
public class ProductController {

    @Autowired
    private SanPhamADMRepository sanPhamADMRepository;

    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @GetMapping("")
    public String product(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("activeMenu", "product");
        model.addAttribute("pageTitle", "Quản lý sản phẩm");
        model.addAttribute("content", "admin/product :: content");

        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("listSanPham",
                    sanPhamADMRepository.findByTenSanPhamContainingIgnoreCase(keyword.trim()));
        } else {
            model.addAttribute("listSanPham", sanPhamADMRepository.findAll());
        }

        model.addAttribute("keyword", keyword);
        return "admin/layout";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("activeMenu", "product");
        model.addAttribute("pageTitle", "Thêm sản phẩm");
        model.addAttribute("content", "admin/product-add :: content");

        model.addAttribute("sanPham", new SanPham());
        model.addAttribute("listThuongHieu", thuongHieuRepository.findAll());
        model.addAttribute("listDanhMuc", danhMucRepository.findAll());

        return "admin/layout";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute SanPham sanPham, RedirectAttributes ra) {

        String ten = sanPham.getTenSanPham() == null ? "" : sanPham.getTenSanPham().trim();
        String ma = sanPham.getMaSanPham() == null ? "" : sanPham.getMaSanPham().trim();
        String chatLieu = sanPham.getChatLieu() == null ? null : sanPham.getChatLieu().trim();

        if (ten.isEmpty()) {
            ra.addFlashAttribute("error", "Tên sản phẩm không được để trống");
            return "redirect:/admin/product/add";
        }

        if (ma.isEmpty()) {
            ra.addFlashAttribute("error", "Mã sản phẩm không được để trống");
            return "redirect:/admin/product/add";
        }

        if (sanPhamADMRepository.existsByTenSanPhamIgnoreCase(ten)) {
            ra.addFlashAttribute("error", "Tên sản phẩm đã tồn tại");
            return "redirect:/admin/product/add";
        }

        if (sanPhamADMRepository.existsByMaSanPhamIgnoreCase(ma)) {
            ra.addFlashAttribute("error", "Mã sản phẩm đã tồn tại");
            return "redirect:/admin/product/add";
        }

        sanPham.setTenSanPham(ten);
        sanPham.setMaSanPham(ma);
        sanPham.setChatLieu((chatLieu == null || chatLieu.isBlank()) ? null : chatLieu);

        if (sanPham.getTrangThai() == null) {
            sanPham.setTrangThai(true);
        }

        sanPhamADMRepository.save(sanPham);

        ra.addFlashAttribute("success", "Thêm sản phẩm thành công");
        return "redirect:/admin/product";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Integer id) {
        SanPham sp = sanPhamADMRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        sp.setTrangThai(!Boolean.TRUE.equals(sp.getTrangThai()));
        sanPhamADMRepository.save(sp);

        return "redirect:/admin/product";
    }
}