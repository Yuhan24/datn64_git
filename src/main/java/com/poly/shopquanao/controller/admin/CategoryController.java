package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.DanhMuc;
import com.poly.shopquanao.repository.admin.DanhMucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private DanhMucRepository danhMucRepository;


    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Danh Mục");
        model.addAttribute("activeGroup", "product_detail");
        model.addAttribute("activeMenu", "category");
        model.addAttribute("content", "admin/category :: content");

        model.addAttribute("listCategory", danhMucRepository.findAll());
        model.addAttribute("category", new DanhMuc());
        return "admin/layout";
    }
    @PostMapping("/save")
    public String save(@ModelAttribute DanhMuc danhMuc, RedirectAttributes ra) {

        String ten = danhMuc.getTenDanhMuc() == null ? "" : danhMuc.getTenDanhMuc().trim();
        if (ten.isEmpty()) {
            ra.addFlashAttribute("error", "Tên thuơng hiệu  không được để trống.");
            return "redirect:/admin/category";
        }

        if (danhMucRepository.existsByTenDanhMucIgnoreCase(ten)) {
            ra.addFlashAttribute("error", "Danh mục \"" + ten + "\" đã tồn tại!");
            return "redirect:/admin/category";
        }

        danhMuc.setTenDanhMuc(ten);
        danhMuc.setTrangThai(true);

        danhMucRepository.save(danhMuc);
        ra.addFlashAttribute("success", "Đã thêm thương hiệu: " + ten);
        return "redirect:/admin/category";
    }


    @PostMapping("/{id}/toggle-status")
    public String toggle(@PathVariable Integer id) {
        DanhMuc Dm = danhMucRepository.findById(id).orElseThrow();
        Dm.setTrangThai(!Boolean.TRUE.equals(Dm.getTrangThai())); // true->false, false/null->true
        danhMucRepository.save(Dm);
        return "redirect:/admin/category";
    }

}
