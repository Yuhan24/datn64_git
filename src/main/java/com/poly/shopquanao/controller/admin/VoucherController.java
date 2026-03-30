package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.KhuyenMai;
import com.poly.shopquanao.repository.admin.KhuyenMaiADRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final KhuyenMaiADRepository khuyenMaiADRepository;

    // =========================
    // Danh sách voucher
    // =========================
    @GetMapping
    public String list(Model model){

        model.addAttribute("title","Khuyến mãi");
        model.addAttribute("pageTitle","Khuyến mãi");
        model.addAttribute("activeMenu","voucher");

        model.addAttribute("list", khuyenMaiADRepository.findAll());

        model.addAttribute("content","admin/voucher :: content");

        return "admin/layout";
    }

    // =========================
    // Form thêm voucher
    // =========================
    @GetMapping("/create")
    public String createForm(Model model){

        model.addAttribute("title","Thêm khuyến mãi");
        model.addAttribute("pageTitle","Thêm khuyến mãi");
        model.addAttribute("activeMenu","voucher");

        model.addAttribute("voucher", new KhuyenMai());

        model.addAttribute("content","admin/voucher-add :: content");

        return "admin/layout";
    }

    // =========================
    // Lưu voucher
    // =========================
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("voucher") KhuyenMai voucher,
                       BindingResult result,
                       Model model){

        if(result.hasErrors()){
            model.addAttribute("content","admin/voucher-add :: content");
            return "admin/layout";
        }

        // Kiểm tra mã trùng
        if(khuyenMaiADRepository.existsByMaKhuyenMai(voucher.getMaKhuyenMai())){
            model.addAttribute("error","Mã khuyến mãi đã tồn tại!");
            model.addAttribute("voucher", voucher);
            model.addAttribute("content","admin/voucher-add :: content");
            return "admin/layout";
        }

        // Kiểm tra giảm %
        if(Boolean.TRUE.equals(voucher.getLoaiGiam())
                && voucher.getGiaTriGiam().compareTo(BigDecimal.valueOf(100)) > 0){

            model.addAttribute("error","Giảm % không được lớn hơn 100%");
            model.addAttribute("voucher", voucher);
            model.addAttribute("content","admin/voucher-add :: content");
            return "admin/layout";
        }

        // Kiểm tra ngày
        if(voucher.getNgayBatDau() != null && voucher.getNgayKetThuc() != null){
            if(voucher.getNgayKetThuc().isBefore(voucher.getNgayBatDau())){
                model.addAttribute("error","Ngày kết thúc phải sau ngày bắt đầu");
                model.addAttribute("voucher", voucher);
                model.addAttribute("content","admin/voucher-add :: content");
                return "admin/layout";
            }
        }

        // trạng thái mặc định
        if(voucher.getTrangThai() == null){
            voucher.setTrangThai(true);
        }

        khuyenMaiADRepository.save(voucher);

        return "redirect:/admin/voucher";
    }

    // =========================
    // Xóa voucher
    // =========================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id){

        if(khuyenMaiADRepository.existsById(id)){
            khuyenMaiADRepository.deleteById(id);
        }

        return "redirect:/admin/voucher";
    }

    // =========================
    // Form sửa
    // =========================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model){

        KhuyenMai voucher = khuyenMaiADRepository.findById(id).orElse(null);

        if(voucher == null){
            return "redirect:/admin/voucher";
        }

        model.addAttribute("voucher", voucher);

        model.addAttribute("title","Sửa khuyến mãi");
        model.addAttribute("pageTitle","Sửa khuyến mãi");
        model.addAttribute("activeMenu","voucher");

        model.addAttribute("content","admin/voucher-edit :: content");

        return "admin/layout";
    }

    // =========================
    // Cập nhật voucher
    // =========================
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("voucher") KhuyenMai formVoucher,
                         BindingResult result,
                         Model model){

        if(result.hasErrors()){
            model.addAttribute("content","admin/voucher-edit :: content");
            return "admin/layout";
        }

        KhuyenMai voucher = khuyenMaiADRepository
                .findById(formVoucher.getId())
                .orElse(null);

        if(voucher == null){
            return "redirect:/admin/voucher";
        }

        // cập nhật dữ liệu
        voucher.setMaKhuyenMai(formVoucher.getMaKhuyenMai());
        voucher.setTenKhuyenMai(formVoucher.getTenKhuyenMai());
        voucher.setLoaiGiam(formVoucher.getLoaiGiam());
        voucher.setGiaTriGiam(formVoucher.getGiaTriGiam());
        voucher.setSoLuong(formVoucher.getSoLuong());
        voucher.setNgayBatDau(formVoucher.getNgayBatDau());
        voucher.setNgayKetThuc(formVoucher.getNgayKetThuc());

        khuyenMaiADRepository.save(voucher);

        return "redirect:/admin/voucher";
    }

    // =========================
    // Bật tắt trạng thái
    // =========================
    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id){

        KhuyenMai voucher = khuyenMaiADRepository.findById(id).orElse(null);

        if(voucher != null){

            if(voucher.getTrangThai() == null){
                voucher.setTrangThai(true);
            }else{
                voucher.setTrangThai(!voucher.getTrangThai());
            }

            khuyenMaiADRepository.save(voucher);
        }

        return "redirect:/admin/voucher";
    }
}