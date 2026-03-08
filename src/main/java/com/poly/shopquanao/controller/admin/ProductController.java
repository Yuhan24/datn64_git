package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.SanPham;

import com.poly.shopquanao.entity.SanPhamHinhAnh;
import com.poly.shopquanao.repository.admin.DanhMucRepository;
import com.poly.shopquanao.repository.admin.HinhAnhRepository;
import com.poly.shopquanao.repository.admin.SanPhamADMRepository;
import com.poly.shopquanao.repository.admin.ThuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/admin/product")
public class ProductController {

    @Autowired
    private SanPhamADMRepository sanPhamADMRepository;

    @Autowired
    ThuongHieuRepository thuongHieuRepository;

    @Autowired
    DanhMucRepository danhMucRepository;

    @Autowired
    HinhAnhRepository hinhAnhRepository;


    @GetMapping("")
    public String product(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("activeMenu", "product");
        model.addAttribute("pageTitle", "Quản lý sản phẩm");
        model.addAttribute("content", "admin/product :: content");

        if (keyword != null && !keyword.trim().isEmpty()){
            model.addAttribute("listSanPham" ,
                    sanPhamADMRepository.findByTenSanPhamContainingIgnoreCase(keyword));
        }else {
            model.addAttribute("listSanPham",sanPhamADMRepository.findAll());
        }

            model.addAttribute("keyword",keyword );

        return "admin/layout.html";
    }
    @GetMapping("/add")
    public String addForm(Model model) {

        model.addAttribute("activeMenu", "product");
        model.addAttribute("pageTitle", "Thêm sản phẩm");
        model.addAttribute("content", "admin/product-add :: content");

        model.addAttribute("sanPham", new SanPham());

        model.addAttribute("listThuongHieu", thuongHieuRepository.findAll());
        model.addAttribute("listDanhMuc", danhMucRepository.findAll());

        return "admin/layout";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute SanPham sanPham,
                       @RequestParam("file") MultipartFile file,
                       RedirectAttributes ra) throws  Exception{

        String ten = sanPham.getTenSanPham().trim();
        String ma = sanPham.getMaSanPham().trim();

        // ❌ check rỗng
        if (ten.isEmpty()) {
            ra.addFlashAttribute("error","Tên sản phẩm không được để trống");
            return "redirect:/admin/product/add";
        }

        if (ma.isEmpty()) {
            ra.addFlashAttribute("error","Mã sản phẩm không được để trống");
            return "redirect:/admin/product/add";
        }

        // ❌ check trùng tên
        if (sanPhamADMRepository.existsByTenSanPhamIgnoreCase(ten)) {
            ra.addFlashAttribute("error","Tên sản phẩm đã tồn tại");
            return "redirect:/admin/product/add";
        }

        // ❌ check trùng mã
        if (sanPhamADMRepository.existsByMaSanPhamIgnoreCase(ma)) {
            ra.addFlashAttribute("error","Mã sản phẩm đã tồn tại");
            return "redirect:/admin/product/add";
        }


        sanPham.setTenSanPham(ten);
        sanPham.setMaSanPham(ma);

        sanPhamADMRepository.save(sanPham);

        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path uploadPath = Paths.get("src/main/resources/static/images");
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            SanPhamHinhAnh img = new SanPhamHinhAnh();
            img.setSanPham(sanPham);
            img.setDuongDanAnh(fileName);

            hinhAnhRepository.save(img);
        }

        ra.addFlashAttribute("success","Thêm sản phẩm thành công");

        return "redirect:/admin/product";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Integer id) {
        SanPham sp = sanPhamADMRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // trangThai: 1 = đang bán, 0 = ngừng bán
        sp.setTrangThai(!Boolean.TRUE.equals(sp.getTrangThai())); // true->false, false/null->true
        sanPhamADMRepository.save(sp);

        return "redirect:/admin/product";
    }



}