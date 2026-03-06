package com.poly.shopquanao.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
public class DashboardController {
    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("title", "dashboard");
        model.addAttribute("pageTitle", "dashboard");
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("content", "admin/dashboard :: content");
        return "admin/layout.html";
    }


}

