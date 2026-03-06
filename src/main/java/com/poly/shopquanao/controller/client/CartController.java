package com.poly.shopquanao.controller.client;


import com.poly.shopquanao.repository.KhachHangLoginRepository;
import com.poly.shopquanao.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final KhachHangLoginRepository khachHangRepo;

    private Integer currentKhId(Authentication authentication) {
        String username = authentication.getName();
        return khachHangRepo.findByTenDangNhap(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"))
                .getId();
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("spId") Integer spChiTietId,
                            @RequestParam("quantity") Integer quantity,
                            Authentication authentication) {

        Integer khId = currentKhId(authentication);
        cartService.addToCart(khId, spChiTietId, quantity);
        System.out.println("[ADD_CART] user=" + authentication.getName()
                + " khId=" + khId
                + " spChiTietId=" + spChiTietId
                + " qty=" + quantity);
        return "redirect:/cart";
    }

    @GetMapping
    public String viewCart(Model model, Authentication authentication) {

        Integer khId = currentKhId(authentication);
        var cartItems = cartService.getCartByKhachHang(khId);

        BigDecimal total = cartItems.stream()
                .map(item -> {
                    BigDecimal price = Optional.ofNullable(item.getSanPhamChiTiet().getGiaBan())
                            .orElse(BigDecimal.ZERO);
                    BigDecimal qty = BigDecimal.valueOf(item.getSoLuong());
                    return price.multiply(qty);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("activeMenu", "cart");

        return "client/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam("id") Integer cartItemId,
                         Authentication authentication) {
        Integer khId = currentKhId(authentication);
        cartService.removeItem(khId, cartItemId);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String update(@RequestParam("id") Integer cartItemId,
                         @RequestParam("quantity") Integer quantity,
                         Authentication authentication) {
        Integer khId = currentKhId(authentication);
        cartService.updateQuantity(khId, cartItemId, quantity);
        return "redirect:/cart";
    }

}