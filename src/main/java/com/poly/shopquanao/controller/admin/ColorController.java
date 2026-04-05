package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.MauSac;
import com.poly.shopquanao.repository.admin.MauSacRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/color")
public class ColorController {
    @Autowired private MauSacRepository mauSacRepository;

    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Màu sắc");
        model.addAttribute("activeGroup", "product_detail");
        model.addAttribute("activeMenu", "color");
        model.addAttribute("content", "admin/color :: content");

        model.addAttribute("listMauSac", mauSacRepository.findAll());
        model.addAttribute("mauSac", new MauSac());
        return "admin/layout";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute MauSac mauSac, RedirectAttributes ra) {

        String ten = mauSac.getTenMau() == null ? "" : mauSac.getTenMau().trim();
        if (ten.isEmpty()) {
            ra.addFlashAttribute("error", "Tên màu không được để trống.");
            return "redirect:/admin/color";
        }

        if (mauSacRepository.existsByTenMauIgnoreCase(ten)) {
            ra.addFlashAttribute("error", "Màu \"" + ten + "\" đã tồn tại!");
            return "redirect:/admin/color";
        }

        mauSac.setTenMau(ten);
        mauSac.setTrangThai(true);

        mauSacRepository.save(mauSac);
        ra.addFlashAttribute("success", "Đã thêm màu: " + ten);
        return "redirect:/admin/color";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggle(@PathVariable Integer id) {
        MauSac ms = mauSacRepository.findById(id).orElseThrow();
        ms.setTrangThai(!Boolean.TRUE.equals(ms.getTrangThai())); // true->false, false/null->true
        mauSacRepository.save(ms);
        return "redirect:/admin/color";
    }


}
