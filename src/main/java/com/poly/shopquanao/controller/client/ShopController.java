package com.poly.shopquanao.controller.client;

import com.poly.shopquanao.service.client.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final SanPhamService sanPhamService;

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("products", sanPhamService.getAllProducts());
        model.addAttribute("activeMenu", "home");
        return "client/home";
    }

    @GetMapping("/product")
    public String product(@RequestParam(required = false) List<Integer> mauIds,
                          @RequestParam(required = false) List<Integer> sizeIds,
                          @RequestParam(required = false) List<String> prices,
                          @RequestParam(required = false) String sort,
                          Model model) {

        model.addAttribute("products", sanPhamService.filterProducts(mauIds, sizeIds, prices, sort));
        model.addAttribute("activeMenu", "product");

        model.addAttribute("selectedMauIds", mauIds);
        model.addAttribute("selectedSizeIds", sizeIds);
        model.addAttribute("selectedPrices", prices);
        model.addAttribute("selectedSort", sort);

        return "client/product";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("activeMenu", "contact");
        return "client/contact";
    }

    @GetMapping("/detail/{id}")
    public String productDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("product", sanPhamService.getProductById(id));
        return "client/product-detail";
    }
}