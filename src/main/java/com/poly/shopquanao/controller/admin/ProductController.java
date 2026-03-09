package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.SanPham;

import com.poly.shopquanao.repository.admin.DanhMucRepository;
import com.poly.shopquanao.repository.admin.SanPhamADMRepository;
import com.poly.shopquanao.repository.admin.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/product")
public class ProductController {

    @Autowired
    private SanPhamADMRepository sanPhamADMRepository;

    @Autowired
    ThuongHieuRepository thuongHieuRepository;

    @Autowired
    DanhMucRepository danhMucRepository;


    @GetMapping("")
    public String product(Model model) {
        model.addAttribute("activeMenu", "product");
        model.addAttribute("pageTitle", "Quản lý sản phẩm");
        model.addAttribute("content", "admin/product :: content");
        model.addAttribute("listSanPham", sanPhamADMRepository.findAll());
        System.out.println(">>> HIT PRODUCT");
        return "admin/layout.html";
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
    public String save(@ModelAttribute SanPham sanPham){

        sanPhamADMRepository.save(sanPham);

        return "redirect:/admin/product";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Integer id) {
        SanPham sp = sanPhamADMRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // trangThai: 1 = đang bán, 0 = ngừng bán
        sp.setTrangThai(!Boolean.TRUE.equals(sp.getTrangThai())); // true->false, false/null->true
        sanPhamADMRepository.save(sp);

        return "redirect:/admin/product";
    }



}