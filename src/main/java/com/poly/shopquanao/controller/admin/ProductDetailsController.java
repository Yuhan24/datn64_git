package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.SanPhamChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.poly.shopquanao.repository.admin.KichCoRepository;
import com.poly.shopquanao.repository.admin.MauSacRepository;
import com.poly.shopquanao.repository.admin.SanPhamChiTietADMRepository;
import com.poly.shopquanao.repository.admin.SanPhamADMRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/product-detail")
public class ProductDetailsController {

    @Autowired
    private SanPhamChiTietADMRepository sanPhamChiTietADMRepository;

    @Autowired
    private SanPhamADMRepository sanPhamADMRepository;

    @Autowired
    private KichCoRepository kichCoRepository;

    @Autowired
    private MauSacRepository mauSacRepository;

    @GetMapping("")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Page<SanPhamChiTiet> pageData = sanPhamChiTietADMRepository.findAll(PageRequest.of(page, size));

        model.addAttribute("pageTitle", "Sản phẩm chi tiết");
        model.addAttribute("activeGroup", "product_detail");
        model.addAttribute("activeMenu", "product_detail");
        model.addAttribute("content", "admin/product-detail :: content");

        model.addAttribute("pageData", pageData);
        model.addAttribute("listSpct", pageData.getContent());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("size", size);

        return "admin/layout";
    }
    @GetMapping("/add")
    public String addForm(Model model){
        model.addAttribute("pageTitle","Thêm sản phẩm chi tiết");
        model.addAttribute("activeGroup","product_detail");
        model.addAttribute("activeMenu","product_detail_add");
        model.addAttribute("content","admin/product-detail-add :: content");

        model.addAttribute("spct",new SanPhamChiTiet());
        model.addAttribute("products", sanPhamADMRepository.findAll());
        model.addAttribute("sizes",kichCoRepository.findByTrangThai(true));
        model.addAttribute("colors",mauSacRepository.findByTrangThai(true));
        return "admin/layout";
    }


    @PostMapping("/save")
    public String save(
            @RequestParam Integer sanPhamId,
            @RequestParam Integer kichCoId,
            @RequestParam Integer mauSacId,
            @RequestParam BigDecimal giaNhap,
            @RequestParam BigDecimal giaBan,
            @RequestParam Integer soLuong,
            @RequestParam(required = false) String moTa,
            RedirectAttributes ra
    ){
        // ❌ Giá nhập phải > 0
        if (giaNhap.compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("error", "Giá nhập phải lớn hơn 0");
            return "redirect:/admin/product-detail/add";
        }

       // Giá bán phải > giá nhập
        if (giaBan.compareTo(giaNhap) <= 0) {
            ra.addFlashAttribute("error", "Giá bán phải lớn hơn giá nhập");
            return "redirect:/admin/product-detail/add";
        }
        // ❌ Số lượng
        if (soLuong < 0) {
            ra.addFlashAttribute("error", "Số lượng không hợp lệ");
            return "redirect:/admin/product-detail/add";
        }


        if (sanPhamChiTietADMRepository.existsBySanPham_IdAndKichCo_IdAndMauSac_Id(sanPhamId , kichCoId , mauSacId)){
            ra.addFlashAttribute("error","Biến thể này đã tồn tại");
            return  "redirect:/admin/product-detail/add";
        }


        SanPhamChiTiet spct = new SanPhamChiTiet();
        spct.setSanPham(sanPhamADMRepository.findById(sanPhamId).orElseThrow());
        spct.setKichCo(kichCoRepository.findById(kichCoId).orElseThrow());
        spct.setMauSac(mauSacRepository.findById(mauSacId).orElseThrow());
        spct.setGiaNhap(giaNhap);
        spct.setGiaBan(giaBan);
        spct.setSoLuong(soLuong);
        spct.setTrangThai(true);
        sanPhamChiTietADMRepository.save(spct);

        ra.addFlashAttribute("success", "Đã thêm SPCT");
        return "redirect:/admin/product-detail";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggle(@PathVariable Integer id,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "5") int size) {

        SanPhamChiTiet x = sanPhamChiTietADMRepository.findById(id).orElseThrow();
        x.setTrangThai(!Boolean.TRUE.equals(x.getTrangThai()));
        sanPhamChiTietADMRepository.save(x);

        return "redirect:/admin/product-detail?page=" + page + "&size=" + size;
    }



}
