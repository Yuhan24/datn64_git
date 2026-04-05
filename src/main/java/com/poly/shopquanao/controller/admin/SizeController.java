package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.KichCo;
import com.poly.shopquanao.repository.admin.KichCoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/size")
public class SizeController {

    @Autowired private KichCoRepository kichCoRepository;

    @GetMapping("")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Kích cỡ");
        model.addAttribute("activeGroup", "product_detail");
        model.addAttribute("activeMenu", "size");
        model.addAttribute("content", "admin/size :: content");

        model.addAttribute("listKichCo", kichCoRepository.findAll());
        model.addAttribute("kichCo", new KichCo());
        return "admin/layout";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute KichCo kichCo, RedirectAttributes ra) {

        String ten = kichCo.getTenKichCo() == null ? "" : kichCo.getTenKichCo().trim();
        if (ten.isEmpty()) {
            ra.addFlashAttribute("error", "Tên kích cỡ không được để trống.");
            return "redirect:/admin/size";
        }

        if (kichCoRepository.existsByTenKichCoIgnoreCase(ten)) {
            ra.addFlashAttribute("error", "Kích cỡ \"" + ten + "\" đã tồn tại!");
            return "redirect:/admin/size";
        }

        kichCo.setTenKichCo(ten);
        kichCo.setTrangThai(true);

        kichCoRepository.save(kichCo);
        ra.addFlashAttribute("success", "Đã thêm kích cỡ: " + ten);
        return "redirect:/admin/size";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggle(@PathVariable Integer id) {
        KichCo kc = kichCoRepository.findById(id).orElseThrow();
        kc.setTrangThai(!Boolean.TRUE.equals(kc.getTrangThai()));
        kichCoRepository.save(kc);
        return "redirect:/admin/size";
    }



}
