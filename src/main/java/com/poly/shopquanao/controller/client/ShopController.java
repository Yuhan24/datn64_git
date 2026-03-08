package com.poly.shopquanao.controller.client;

import com.poly.shopquanao.repository.KhachHangLoginRepository;
import com.poly.shopquanao.repository.client.GioHangChiTietRepository;
import com.poly.shopquanao.repository.client.KichCoClientRepository;
import com.poly.shopquanao.repository.client.MauSacClientRepository;
import com.poly.shopquanao.service.client.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final SanPhamService sanPhamService;
    private final MauSacClientRepository mauSacClientRepository;
    private final KichCoClientRepository kichCoClientRepository;
    private final KhachHangLoginRepository khachHangLoginRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;

    @ModelAttribute("mauSacs")
    public List<?> loadMauSacs() {
        return mauSacClientRepository.findByTrangThaiTrue();
    }

    @ModelAttribute("kichCos")
    public List<?> loadKichCos() {
        return kichCoClientRepository.findByTrangThaiTrue();
    }

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("products", sanPhamService.getAllProducts());
        model.addAttribute("activeMenu", "home");
        return "client/home";
    }

    @GetMapping("/product")
    public String product(@RequestParam(required = false) List<Integer> mauIds,
                          @RequestParam(required = false) List<Integer> sizeIds,
                          @RequestParam(required = false) String price,
                          @RequestParam(required = false) String sort,
                          Model model) {

        model.addAttribute("products", sanPhamService.filterProducts(mauIds, sizeIds, price, sort));
        model.addAttribute("activeMenu", "product");

        model.addAttribute("selectedMauIds", mauIds);
        model.addAttribute("selectedSizeIds", sizeIds);
        model.addAttribute("selectedPrice", price);
        model.addAttribute("selectedSort", sort);

        return "client/product";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("activeMenu", "contact");
        return "client/contact";
    }

    @GetMapping("/detail/{id}")
    public String productDetail(@PathVariable Integer id,
                                Model model,
                                Authentication authentication) {

        var product = sanPhamService.getProductById(id);
        model.addAttribute("product", product);

        Map<Integer, Integer> cartQtyMap = new HashMap<>();

        if (authentication != null) {
            String username = authentication.getName();

            Integer khId = khachHangLoginRepository.findByTenDangNhap(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"))
                    .getId();

            for (var ct : product.getChiTietList()) {
                Integer soLuongTrongGio = gioHangChiTietRepository
                        .findSoLuongTrongGio(khId, ct.getId());

                cartQtyMap.put(ct.getId(), soLuongTrongGio == null ? 0 : soLuongTrongGio);
            }
        }

        model.addAttribute("cartQtyMap", cartQtyMap);

        return "client/product-detail";
    }
}