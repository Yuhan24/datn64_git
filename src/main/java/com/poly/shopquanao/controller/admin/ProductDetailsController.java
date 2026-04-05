package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.*;
import com.poly.shopquanao.repository.admin.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

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
        model.addAttribute("sizes", kichCoRepository.findByTrangThaiTrue());
        model.addAttribute("colors", mauSacRepository.findByTrangThaiTrue());

        model.addAttribute("pageTitle", "Thêm sản phẩm chi tiết");
        model.addAttribute("activeGroup", "product_detail");
        model.addAttribute("activeMenu", "product_detail");
        model.addAttribute("content", "admin/product-detail-add :: content");

        return "admin/layout";
    }

    @PostMapping("/save")
    @Transactional
    public String save(@RequestParam Integer sanPhamId,
                       @RequestParam List<Integer> kichCoIds,
                       @RequestParam List<Integer> mauSacIds,
                       @RequestParam BigDecimal giaNhap,
                       @RequestParam BigDecimal giaBan,
                       @RequestParam Integer soLuong,
                       @RequestParam(value = "images", required = false) MultipartFile[] images,
                       RedirectAttributes ra) {

        try {
            if (sanPhamId == null || kichCoIds == null || kichCoIds.isEmpty()
                    || mauSacIds == null || mauSacIds.isEmpty()) {
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
                ra.addFlashAttribute("error", "Số lượng phải lớn hơn 0");
                return "redirect:/admin/product-detail/add";
            }

            SanPham sanPham = sanPhamADMRepository.findById(sanPhamId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            int soBienTheMoi = 0;
            int soBienTheCapNhat = 0;

            for (Integer mauSacId : mauSacIds) {
                MauSac mauSac = mauSacRepository.findById(mauSacId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc: " + mauSacId));

                // Ảnh đang đi theo màu -> chỉ lưu 1 lần cho mỗi màu
                if (hasRealFiles(images)) {
                    replaceImagesByProductAndColor(sanPham, mauSac, images);
                }

                for (Integer kichCoId : kichCoIds) {
                    KichCo kichCo = kichCoRepository.findById(kichCoId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy kích cỡ: " + kichCoId));

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
                        soBienTheCapNhat++;
                    } else {
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
                        soBienTheMoi++;
                    }
                }
            }

            ra.addFlashAttribute(
                    "success",
                    "Tạo mới " + soBienTheMoi + " biến thể, cập nhật " + soBienTheCapNhat + " biến thể"
            );
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