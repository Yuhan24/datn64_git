package com.poly.shopquanao.controller.admin;


import com.poly.shopquanao.entity.ThuongHieu;
import com.poly.shopquanao.repository.admin.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/brand")
public class BrandController {

    @Autowired
    private   ThuongHieuRepository thuongHieuRepository;


    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Thương hiệu");
        model.addAttribute("activeGroup", "product_detail");
        model.addAttribute("activeMenu", "brand");
        model.addAttribute("content", "admin/brand :: content");

        model.addAttribute("listBrand", thuongHieuRepository.findAll());
        model.addAttribute("brand", new ThuongHieu());
        return "admin/layout";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ThuongHieu thuongHieu, RedirectAttributes ra) {

        String ten = thuongHieu.getTenThuongHieu() == null ? "" : thuongHieu.getTenThuongHieu().trim();
        if (ten.isEmpty()) {
            ra.addFlashAttribute("error", "Tên thuơng hiệu  không được để trống.");
            return "redirect:/admin/brand";
        }

        if (thuongHieuRepository.existsByTenThuongHieuIgnoreCase(ten)) {
            ra.addFlashAttribute("error", "Thương hiệu \"" + ten + "\" đã tồn tại!");
            return "redirect:/admin/brand";
        }

        thuongHieu.setTenThuongHieu(ten);
        thuongHieu.setTrangThai(true);

        thuongHieuRepository.save(thuongHieu);
        ra.addFlashAttribute("success", "Đã thêm thương hiệu: " + ten);
        return "redirect:/admin/brand";
    }


    @PostMapping("/{id}/toggle-status")
    public String toggle(@PathVariable Integer id) {
        ThuongHieu th = thuongHieuRepository.findById(id).orElseThrow();
        th.setTrangThai(!Boolean.TRUE.equals(th.getTrangThai())); // true->false, false/null->true
        thuongHieuRepository.save(th);
        return "redirect:/admin/brand";
    }

}
