package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.DonHang;
import com.poly.shopquanao.entity.DonHangChiTiet;
import com.poly.shopquanao.repository.admin.DonHangADRepository;
import com.poly.shopquanao.repository.admin.DonHangChiTietADRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/order-off")
public class OrderOfflineController {

    @Autowired
    DonHangADRepository donHangADRepository;

    @Autowired
    DonHangChiTietADRepository donHangChiTietADRepository;

    @GetMapping("")
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model){
        Page<DonHang> pageData =donHangADRepository.findByLoaiDonOrderByNgayTaoDesc(1, PageRequest.of(page,size));
        model.addAttribute("pageTitle", "Hóa đơn tại quầy");
        model.addAttribute("activeGroup", "order");
        model.addAttribute("activeMenu", "order-off");
        model.addAttribute("content", "admin/order-off :: content");

        model.addAttribute("listOrder" ,pageData.getContent());
        model.addAttribute("currentPage",page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("size",size);


        return "admin/layout";

    }

    @GetMapping("/detail/{id}")
    public String detail(
            @PathVariable Integer id,
            Model model
    ){
        DonHang donHang = donHangADRepository.findById(id).orElseThrow();
        List<DonHangChiTiet> chiTietList = donHangChiTietADRepository.findByDonHang_Id(id);
        model.addAttribute("pageTitle", "Chi tiết hóa đơn tại quầy");
        model.addAttribute("activeGroup", "order");
        model.addAttribute("activeMenu", "order-off");
        model.addAttribute("content", "admin/order-off-detail :: content");

        model.addAttribute("donHang", donHang);
        model.addAttribute("chiTietList", chiTietList);

        return "admin/layout";
    }


    @GetMapping("/print/{id}")
    public String print(
            @PathVariable Integer id,
            Model model
    ){
        DonHang donHang = donHangADRepository.findById(id).orElseThrow();
        List<DonHangChiTiet> chiTietList = donHangChiTietADRepository.findByDonHang_Id(id);
        model.addAttribute("donHang",donHang);
        model.addAttribute("chiTietList",chiTietList);
        return "admin/order-print";


    }


}
