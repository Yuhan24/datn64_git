package com.poly.shopquanao.controller.client;

import com.poly.shopquanao.dto.client.CheckoutRequest;
import com.poly.shopquanao.entity.*;
import com.poly.shopquanao.repository.KhachHangLoginRepository;
import com.poly.shopquanao.repository.client.DonHangRepository;
import com.poly.shopquanao.repository.client.GioHangChiTietRepository;
import com.poly.shopquanao.repository.client.GioHangRepository;
import com.poly.shopquanao.repository.client.SanPhamChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderClientController {

    private final GioHangRepository gioHangRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final DonHangRepository donHangRepository;
    private final KhachHangLoginRepository khachHangRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    private KhachHang getCurrentKhachHang(Authentication authentication) {
        String username = authentication.getName();
        return khachHangRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
    }

    private GioHang getCurrentGioHang(KhachHang khachHang) {
        return gioHangRepository.findByKhachHang_Id(khachHang.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));
    }

    private BigDecimal tinhTongTien(List<GioHangChiTiet> items) {
        return items.stream()
                .map(item -> {
                    BigDecimal gia = item.getSanPhamChiTiet().getGiaBan() != null
                            ? item.getSanPhamChiTiet().getGiaBan()
                            : BigDecimal.ZERO;
                    return gia.multiply(BigDecimal.valueOf(item.getSoLuong()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @PostMapping("/checkout")
    public String checkoutAll(Authentication authentication, Model model) {
        KhachHang khachHang = getCurrentKhachHang(authentication);
        GioHang gioHang = getCurrentGioHang(khachHang);

        List<GioHangChiTiet> items = new ArrayList<>(gioHang.getChiTietList());
        if (items.isEmpty()) {
            return "redirect:/cart?empty";
        }

        model.addAttribute("checkoutItems", items);
        model.addAttribute("tongTien", tinhTongTien(items));
        model.addAttribute("khachHang", khachHang);

        return "client/checkout";
    }

    @PostMapping("/checkout-one")
    public String checkoutOne(@RequestParam("cartItemId") Integer cartItemId,
                              Authentication authentication,
                              Model model) {
        KhachHang khachHang = getCurrentKhachHang(authentication);
        GioHang gioHang = getCurrentGioHang(khachHang);

        GioHangChiTiet item = gioHangChiTietRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        if (!item.getGioHang().getId().equals(gioHang.getId())) {
            return "redirect:/cart?invalidItem";
        }

        List<GioHangChiTiet> items = List.of(item);

        model.addAttribute("checkoutItems", items);
        model.addAttribute("tongTien", tinhTongTien(items));
        model.addAttribute("khachHang", khachHang);

        return "client/checkout";
    }

    @PostMapping("/checkout-selected")
    public String checkoutSelected(@RequestParam("selectedIds") List<Integer> selectedIds,
                                   Authentication authentication,
                                   Model model) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return "redirect:/cart?noSelected";
        }

        KhachHang khachHang = getCurrentKhachHang(authentication);
        GioHang gioHang = getCurrentGioHang(khachHang);

        List<GioHangChiTiet> allItems = new ArrayList<>(gioHang.getChiTietList());

        List<GioHangChiTiet> selectedItems = allItems.stream()
                .filter(item -> selectedIds.contains(item.getId()))
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            return "redirect:/cart?noSelected";
        }

        model.addAttribute("checkoutItems", selectedItems);
        model.addAttribute("tongTien", tinhTongTien(selectedItems));
        model.addAttribute("khachHang", khachHang);

        return "client/checkout";
    }

    @PostMapping("/place")
    @Transactional
    public String placeOrder(@ModelAttribute CheckoutRequest request,
                             Authentication authentication) {

        if (request.getSelectedIds() == null || request.getSelectedIds().isEmpty()) {
            return "redirect:/cart?noSelected";
        }

        KhachHang khachHang = getCurrentKhachHang(authentication);
        GioHang gioHang = getCurrentGioHang(khachHang);

        List<GioHangChiTiet> allItems = new ArrayList<>(gioHang.getChiTietList());

        List<GioHangChiTiet> selectedItems = allItems.stream()
                .filter(item -> request.getSelectedIds().contains(item.getId()))
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            return "redirect:/cart?noSelected";
        }

        BigDecimal tongTien = BigDecimal.ZERO;
        List<DonHangChiTiet> chiTietDonHangList = new ArrayList<>();

        for (GioHangChiTiet item : selectedItems) {
            SanPhamChiTiet spct = item.getSanPhamChiTiet();

            if (spct.getSoLuong() == null || spct.getSoLuong() < item.getSoLuong()) {
                return "redirect:/cart?outOfStock";
            }

            BigDecimal giaBan = spct.getGiaBan() != null ? spct.getGiaBan() : BigDecimal.ZERO;
            tongTien = tongTien.add(giaBan.multiply(BigDecimal.valueOf(item.getSoLuong())));
        }

        DonHang donHang = new DonHang();
        donHang.setMaDonHang("DH" + System.currentTimeMillis());
        donHang.setKhachHang(khachHang);
        donHang.setTenNguoiNhan(request.getTenNguoiNhan());
        donHang.setSoDienThoai(request.getSoDienThoai());
        donHang.setDiaChiGiaoHang(request.getDiaChiGiaoHang());
        donHang.setGhiChu(request.getGhiChu());
        donHang.setPhuongThucThanhToan(request.getPhuongThucThanhToan());
        donHang.setTongTien(tongTien);
        donHang.setTienGiam(BigDecimal.ZERO);
        donHang.setTrangThaiId(1);

        if ("COD".equals(request.getPhuongThucThanhToan())) {
            donHang.setTrangThaiThanhToan("CHUA_THANH_TOAN");
        } else {
            donHang.setTrangThaiThanhToan("CHO_THANH_TOAN");
        }

        for (GioHangChiTiet item : selectedItems) {
            SanPhamChiTiet spct = item.getSanPhamChiTiet();

            DonHangChiTiet ct = new DonHangChiTiet();
            ct.setDonHang(donHang);
            ct.setSanPhamChiTiet(spct);
            ct.setSoLuong(item.getSoLuong());
            ct.setGiaTaiThoiDiem(spct.getGiaBan());

            chiTietDonHangList.add(ct);

            spct.setSoLuong(spct.getSoLuong() - item.getSoLuong());
            sanPhamChiTietRepository.save(spct);
        }

        donHang.setChiTietList(chiTietDonHangList);
        donHangRepository.save(donHang);

        gioHangChiTietRepository.deleteAll(selectedItems);

        return "redirect:/order/success/" + donHang.getId();
    }

    @GetMapping("/success/{id}")
    public String success(@PathVariable Integer id, Model model) {
        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        model.addAttribute("order", donHang);
        return "client/order-success";
    }

    @GetMapping("/my-orders")
    public String myOrders(Authentication authentication, Model model) {

        KhachHang khachHang = getCurrentKhachHang(authentication);

        List<DonHang> orders =
                donHangRepository.findByKhachHang_IdOrderByNgayTaoDesc(khachHang.getId());

        model.addAttribute("orders", orders);
        model.addAttribute("activeMenu", "orders");

        return "client/my-orders";
    }
}