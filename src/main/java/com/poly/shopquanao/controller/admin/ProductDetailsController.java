package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.KichCo;
import com.poly.shopquanao.entity.MauSac;
import com.poly.shopquanao.entity.SanPham;
import com.poly.shopquanao.entity.SanPhamChiTiet;
import com.poly.shopquanao.entity.SanPhamHinhAnh;
import com.poly.shopquanao.repository.admin.KichCoRepository;
import com.poly.shopquanao.repository.admin.MauSacRepository;
import com.poly.shopquanao.repository.admin.SanPhamADMRepository;
import com.poly.shopquanao.repository.admin.SanPhamChiTietADMRepository;
import com.poly.shopquanao.repository.admin.SanPhamHinhAnhRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin/product-detail")
@RequiredArgsConstructor
public class ProductDetailsController {

    private final SanPhamChiTietADMRepository sanPhamChiTietADMRepository;
    private final SanPhamADMRepository sanPhamADMRepository;
    private final KichCoRepository kichCoRepository;
    private final MauSacRepository mauSacRepository;
    private final SanPhamHinhAnhRepository sanPhamHinhAnhRepository;

    @GetMapping("")
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                        Model model) {

        Page<SanPhamChiTiet> pageData = sanPhamChiTietADMRepository.findAll(PageRequest.of(page, size));

        model.addAttribute("listSpct", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("size", size);

        model.addAttribute("pageTitle", "Sản phẩm chi tiết");
        model.addAttribute("activeGroup", "product_detail");
        model.addAttribute("activeMenu", "product_detail");
        model.addAttribute("content", "admin/product-detail :: content");

        return "admin/layout";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("products", sanPhamADMRepository.findAll());
        model.addAttribute("sizes", kichCoRepository.findAll());
        model.addAttribute("colors", mauSacRepository.findAll());

        model.addAttribute("pageTitle", "Thêm sản phẩm chi tiết");
        model.addAttribute("activeGroup", "product_detail");
        model.addAttribute("activeMenu", "product_detail");
        model.addAttribute("content", "admin/product-detail-add :: content");

        return "admin/layout";
    }

    @PostMapping("/save")
    @Transactional
    public String save(@RequestParam Integer sanPhamId,
                       @RequestParam Integer kichCoId,
                       @RequestParam Integer mauSacId,
                       @RequestParam BigDecimal giaNhap,
                       @RequestParam BigDecimal giaBan,
                       @RequestParam Integer soLuong,
                       @RequestParam(value = "images", required = false) MultipartFile[] images,
                       RedirectAttributes ra) {

        try {
            if (sanPhamId == null || kichCoId == null || mauSacId == null) {
                ra.addFlashAttribute("error", "Thiếu dữ liệu sản phẩm / kích cỡ / màu sắc");
                return "redirect:/admin/product-detail/add";
            }

            if (giaNhap == null || giaNhap.compareTo(BigDecimal.ONE) < 0) {
                ra.addFlashAttribute("error", "Giá nhập phải lớn hơn 0");
                return "redirect:/admin/product-detail/add";
            }

            if (giaBan == null || giaBan.compareTo(BigDecimal.ONE) < 0) {
                ra.addFlashAttribute("error", "Giá bán phải lớn hơn 0");
                return "redirect:/admin/product-detail/add";
            }

            if (giaBan.compareTo(giaNhap) <= 0) {
                ra.addFlashAttribute("error", "Giá bán phải lớn hơn giá nhập");
                return "redirect:/admin/product-detail/add";
            }

            if (soLuong == null || soLuong <= 0) {
                ra.addFlashAttribute("error", "Số lượng nhập thêm phải lớn hơn 0");
                return "redirect:/admin/product-detail/add";
            }

            SanPham sanPham = sanPhamADMRepository.findById(sanPhamId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            KichCo kichCo = kichCoRepository.findById(kichCoId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy kích cỡ"));

            MauSac mauSac = mauSacRepository.findById(mauSacId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"));

            Optional<SanPhamChiTiet> existingOpt = sanPhamChiTietADMRepository
                    .findBySanPham_IdAndKichCo_IdAndMauSac_Id(sanPhamId, kichCoId, mauSacId);

            if (existingOpt.isPresent()) {
                SanPhamChiTiet spct = existingOpt.get();

                int soLuongCu = spct.getSoLuong() == null ? 0 : spct.getSoLuong();
                spct.setSoLuong(soLuongCu + soLuong);

                spct.setGiaNhap(giaNhap);
                spct.setGiaBan(giaBan);
                spct.setTrangThai(true);

                sanPhamChiTietADMRepository.save(spct);

                if (hasRealFiles(images)) {
                    replaceImagesByProductAndColor(sanPham, mauSac, images);
                }

                ra.addFlashAttribute("success", "Đã nhập thêm hàng cho biến thể đã tồn tại");
                return "redirect:/admin/product-detail";
            }

            SanPhamChiTiet spct = SanPhamChiTiet.builder()
                    .sanPham(sanPham)
                    .kichCo(kichCo)
                    .mauSac(mauSac)
                    .giaNhap(giaNhap)
                    .giaBan(giaBan)
                    .soLuong(soLuong)
                    .trangThai(true)
                    .build();

            sanPhamChiTietADMRepository.save(spct);

            if (hasRealFiles(images)) {
                replaceImagesByProductAndColor(sanPham, mauSac, images);
            }

            ra.addFlashAttribute("success", "Thêm sản phẩm chi tiết thành công");
            return "redirect:/admin/product-detail";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi lưu sản phẩm chi tiết: " + e.getMessage());
            return "redirect:/admin/product-detail/add";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Integer id,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size) {

        SanPhamChiTiet spct = sanPhamChiTietADMRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

        spct.setTrangThai(!Boolean.TRUE.equals(spct.getTrangThai()));
        sanPhamChiTietADMRepository.save(spct);

        return "redirect:/admin/product-detail?page=" + page + "&size=" + size;
    }

    private boolean hasRealFiles(MultipartFile[] files) {
        return files != null
                && files.length > 0
                && Arrays.stream(files).anyMatch(f -> f != null && !f.isEmpty());
    }

    private void replaceImagesByProductAndColor(SanPham sanPham,
                                                MauSac mauSac,
                                                MultipartFile[] images) throws IOException {

        sanPhamHinhAnhRepository.deleteBySanPham_IdAndMauSac_Id(sanPham.getId(), mauSac.getId());

        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String fileName = saveImageFile(file);

            SanPhamHinhAnh hinhAnh = SanPhamHinhAnh.builder()
                    .sanPham(sanPham)
                    .mauSac(mauSac)
                    .duongDanAnh(fileName)
                    .build();

            sanPhamHinhAnhRepository.save(hinhAnh);
        }
    }

    private String saveImageFile(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = "";

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String newFileName = UUID.randomUUID() + extension;

        Path uploadDir = Paths.get("uploads/images").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        Path targetPath = uploadDir.resolve(newFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return newFileName;
    }
}