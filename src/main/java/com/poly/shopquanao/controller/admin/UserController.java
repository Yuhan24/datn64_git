package com.poly.shopquanao.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller cũ "Người dùng" – giờ redirect sang trang Khách hàng mới.
 * Giữ lại để tránh lỗi 404 nếu có link cũ trỏ đến /admin/user.
 */
@Controller
@RequestMapping("/admin/user")
public class UserController {

    @GetMapping("")
    public String user() {
        // Redirect sang trang quản lý khách hàng mới
        return "redirect:/admin/customer";
    }
}
