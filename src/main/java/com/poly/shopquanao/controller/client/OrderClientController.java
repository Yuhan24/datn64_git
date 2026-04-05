package com.poly.shopquanao.controller.client;

import com.poly.shopquanao.dto.client.CheckoutRequest;
import com.poly.shopquanao.entity.*;
import com.poly.shopquanao.repository.KhachHangLoginRepository;
import com.poly.shopquanao.repository.client.*;
import com.poly.shopquanao.service.client.GoongShippingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderClientController {

    private static final String SESSION_CHECKOUT_SELECTED_IDS = "CHECKOUT_SELECTED_IDS";

    private final GoongShippingService goongShippingService;
    private final KhuyenMaiClientRepository khuyenMaiClientRepository;
    private final GioHangRepository gioHangRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final DonHangRepository donHangRepository;
    private final KhachHangLoginRepository khachHangRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    private static class CouponResult {
        private final KhuyenMai khuyenMai;
        private final BigDecimal tienGiam;
        private final String couponError;
        private final boolean couponSuccess;

        public CouponResult(KhuyenMai khuyenMai,
                            BigDecimal tienGiam,
                            String couponError,
                            boolean couponSuccess) {
            this.khuyenMai = khuyenMai;
            this.tienGiam = tienGiam;
            this.couponError = couponError;
            this.couponSuccess = couponSuccess;
        }

        public KhuyenMai getKhuyenMai() {
            return khuyenMai;
        }

        public BigDecimal getTienGiam() {
            return tienGiam;
        }

        public String getCouponError() {
            return couponError;
        }

        public boolean isCouponSuccess() {
            return couponSuccess;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private CouponResult validateCoupon(String maKhuyenMai, BigDecimal tongTienGoc) {
        if (maKhuyenMai == null || maKhuyenMai.isBlank()) {
            return new CouponResult(null, BigDecimal.ZERO, null, false);
        }

        String maNhap = maKhuyenMai.trim();
        KhuyenMai khuyenMai = khuyenMaiClientRepository.findByMaKhuyenMai(maNhap).orElse(null);

        if (khuyenMai == null) {
            return new CouponResult(null, BigDecimal.ZERO, "not_found", false);
        }

        LocalDateTime now = LocalDateTime.now();

        if (khuyenMai.getNgayBatDau() != null && now.isBefore(khuyenMai.getNgayBatDau())) {
            return new CouponResult(null, BigDecimal.ZERO, "not_started", false);
        }

        if (khuyenMai.getNgayKetThuc() != null && now.isAfter(khuyenMai.getNgayKetThuc())) {
            return new CouponResult(null, BigDecimal.ZERO, "expired", false);
        }

        if (!Boolean.TRUE.equals(khuyenMai.getTrangThai())) {
            return new CouponResult(null, BigDecimal.ZERO, "inactive", false);
        }

        if (khuyenMai.getSoLuong() == null || khuyenMai.getSoLuong() <= 0) {
            return new CouponResult(null, BigDecimal.ZERO, "out_of_stock", false);
        }

        BigDecimal giaTriDonToiThieu = khuyenMai.getGiaTriDonToiThieu() != null
                ? khuyenMai.getGiaTriDonToiThieu()
                : BigDecimal.ZERO;

        if (tongTienGoc.compareTo(giaTriDonToiThieu) < 0) {
            return new CouponResult(null, BigDecimal.ZERO, "min_order", false);
        }

        BigDecimal tienGiam;
        if (Boolean.TRUE.equals(khuyenMai.getLoaiGiam())) {
            tienGiam = tongTienGoc
                    .multiply(khuyenMai.getGiaTriGiam())
                    .divide(BigDecimal.valueOf(100));
        } else {
            tienGiam = khuyenMai.getGiaTriGiam();
        }

        if (tienGiam.compareTo(tongTienGoc) > 0) {
            tienGiam = tongTienGoc;
        }

        return new CouponResult(khuyenMai, tienGiam, null, true);
    }

    private KhachHang getCurrentKhachHang(Authentication authentication) {
        String username = authentication.getName();
        return khachHangRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng"));
    }

    private GioHang getCurrentGioHang(KhachHang khachHang) {
        return gioHangRepository.findByKhachHang_Id(khachHang.getId())
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y giá» hÃ ng"));
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

    private BigDecimal tinhTongTienDonHang(DonHang donHang) {
        if (donHang == null || donHang.getChiTietList() == null) {
            return BigDecimal.ZERO;
        }

        return donHang.getChiTietList().stream()
                .map(item -> {
                    BigDecimal gia = item.getGiaTaiThoiDiem() != null
                            ? item.getGiaTaiThoiDiem()
                            : BigDecimal.ZERO;
                    int soLuong = item.getSoLuong() != null ? item.getSoLuong() : 0;
                    return gia.multiply(BigDecimal.valueOf(soLuong));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Map<String, Object>> buildVoucherList(BigDecimal tongTienGoc, String selectedCode) {
        LocalDateTime now = LocalDateTime.now();

        return khuyenMaiClientRepository.findAll().stream()
                .map(km -> {
                    String trangThai;
                    boolean coTheChon = false;

                    if (!Boolean.TRUE.equals(km.getTrangThai())) {
                        trangThai = "KhÃ´ng hoáº¡t Ä‘á»™ng";
                    } else if (km.getNgayBatDau() != null && now.isBefore(km.getNgayBatDau())) {
                        trangThai = "ChÆ°a báº¯t Ä‘áº§u";
                    } else if (km.getNgayKetThuc() != null && now.isAfter(km.getNgayKetThuc())) {
                        trangThai = "Háº¿t háº¡n";
                    } else if (km.getSoLuong() == null || km.getSoLuong() <= 0) {
                        trangThai = "Háº¿t lÆ°á»£t";
                    } else if (km.getGiaTriDonToiThieu() != null
                            && tongTienGoc.compareTo(km.getGiaTriDonToiThieu()) < 0) {
                        trangThai = "ChÆ°a Ä‘á»§ Ä‘iá»u kiá»‡n";
                    } else {
                        trangThai = "CÃ³ thá»ƒ dÃ¹ng";
                        coTheChon = true;
                    }

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("km", km);
                    item.put("trangThai", trangThai);
                    item.put("coTheChon", coTheChon);
                    item.put("duocChon", selectedCode != null
                            && !selectedCode.isBlank()
                            && selectedCode.trim().equalsIgnoreCase(km.getMaKhuyenMai()));

                    return item;
                })
                .sorted((a, b) -> {
                    boolean aUsable = Boolean.TRUE.equals(a.get("coTheChon"));
                    boolean bUsable = Boolean.TRUE.equals(b.get("coTheChon"));
                    return Boolean.compare(bUsable, aUsable);
                })
                .toList();
    }

    private void saveSelectedIdsToSession(HttpSession session, List<Integer> selectedIds) {
        session.setAttribute(SESSION_CHECKOUT_SELECTED_IDS, new ArrayList<>(selectedIds));
    }

    @SuppressWarnings("unchecked")
    private List<Integer> getSelectedIdsFromSession(HttpSession session) {
        Object value = session.getAttribute(SESSION_CHECKOUT_SELECTED_IDS);
        if (value instanceof List<?>) {
            return ((List<?>) value).stream()
                    .filter(Objects::nonNull)
                    .map(v -> Integer.valueOf(v.toString()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void clearCheckoutSession(HttpSession session) {
        session.removeAttribute(SESSION_CHECKOUT_SELECTED_IDS);
    }

    private List<GioHangChiTiet> getSelectedItemsForCheckout(Authentication authentication, HttpSession session) {
        KhachHang khachHang = getCurrentKhachHang(authentication);
        GioHang gioHang = getCurrentGioHang(khachHang);

        List<Integer> selectedIds = getSelectedIdsFromSession(session);
        if (selectedIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<GioHangChiTiet> allItems = new ArrayList<>(gioHang.getChiTietList());

        return allItems.stream()
                .filter(item -> selectedIds.contains(item.getId()))
                .collect(Collectors.toList());
    }

    @PostMapping("/checkout")
    public String checkoutAll(Authentication authentication, HttpSession session) {
        KhachHang khachHang = getCurrentKhachHang(authentication);
        GioHang gioHang = getCurrentGioHang(khachHang);

        List<GioHangChiTiet> items = new ArrayList<>(gioHang.getChiTietList());
        if (items.isEmpty()) {
            clearCheckoutSession(session);
            return "redirect:/cart?empty";
        }

        List<Integer> selectedIds = items.stream()
                .map(GioHangChiTiet::getId)
                .collect(Collectors.toList());

        saveSelectedIdsToSession(session, selectedIds);
        return "redirect:/order/checkout";
    }

    @PostMapping("/checkout-one")
    public String checkoutOne(@RequestParam("cartItemId") Integer cartItemId,
                              Authentication authentication,
                              HttpSession session) {

        KhachHang khachHang = getCurrentKhachHang(authentication);
        GioHang gioHang = getCurrentGioHang(khachHang);

        GioHangChiTiet item = gioHangChiTietRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m trong giá»"));

        if (!item.getGioHang().getId().equals(gioHang.getId())) {
            clearCheckoutSession(session);
            return "redirect:/cart?invalidItem";
        }

        saveSelectedIdsToSession(session, List.of(item.getId()));
        return "redirect:/order/checkout";
    }

    @PostMapping("/checkout-selected")
    public String checkoutSelected(@RequestParam("selectedIds") List<Integer> selectedIds,
                                   Authentication authentication,
                                   HttpSession session) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            clearCheckoutSession(session);
            return "redirect:/cart?noSelected";
        }

        KhachHang khachHang = getCurrentKhachHang(authentication);
        GioHang gioHang = getCurrentGioHang(khachHang);

        List<GioHangChiTiet> allItems = new ArrayList<>(gioHang.getChiTietList());

        List<Integer> validSelectedIds = allItems.stream()
                .filter(item -> selectedIds.contains(item.getId()))
                .map(GioHangChiTiet::getId)
                .collect(Collectors.toList());

        if (validSelectedIds.isEmpty()) {
            clearCheckoutSession(session);
            return "redirect:/cart?noSelected";
        }

        saveSelectedIdsToSession(session, validSelectedIds);
        return "redirect:/order/checkout";
    }

    @GetMapping("/checkout")
    public String checkoutPage(Authentication authentication,
                               HttpSession session,
                               Model model) {
        KhachHang khachHang = getCurrentKhachHang(authentication);
        List<GioHangChiTiet> items = getSelectedItemsForCheckout(authentication, session);

        if (items.isEmpty()) {
            clearCheckoutSession(session);
            return "redirect:/cart?noSelected";
        }

        BigDecimal tongTien = tinhTongTien(items);

        return renderCheckoutPage(
                model, khachHang, items,
                null, null, false,
                tongTien, BigDecimal.ZERO, BigDecimal.ZERO, tongTien
        );
    }

    @PostMapping("/place")
    @Transactional
    public String placeOrder(@ModelAttribute CheckoutRequest request,
                             @RequestParam(value = "action", required = false) String action,
                             Authentication authentication,
                             HttpSession session,
                             Model model) {

        if (action == null) {
            action = "placeOrder";
        }

        if (request.getSelectedIds() == null || request.getSelectedIds().isEmpty()) {
            List<Integer> sessionSelectedIds = getSelectedIdsFromSession(session);
            if (!sessionSelectedIds.isEmpty()) {
                request.setSelectedIds(sessionSelectedIds);
            }
        }

        if (request.getSelectedIds() == null || request.getSelectedIds().isEmpty()) {
            clearCheckoutSession(session);
            return "redirect:/cart?noSelected";
        }

        KhachHang khachHang = getCurrentKhachHang(authentication);
        GioHang gioHang = getCurrentGioHang(khachHang);

        List<GioHangChiTiet> allItems = new ArrayList<>(gioHang.getChiTietList());

        List<GioHangChiTiet> selectedItems = allItems.stream()
                .filter(item -> request.getSelectedIds().contains(item.getId()))
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            clearCheckoutSession(session);
            return "redirect:/cart?noSelected";
        }

        BigDecimal tongTienGoc = BigDecimal.ZERO;
        List<DonHangChiTiet> chiTietDonHangList = new ArrayList<>();
        Map<Integer, SanPhamChiTiet> lockedSpctMap = new HashMap<>();

        for (GioHangChiTiet item : selectedItems) {
            Integer spctId = item.getSanPhamChiTiet().getId();

            SanPhamChiTiet spct = sanPhamChiTietRepository.findByIdForUpdate(spctId)
                    .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m"));

            lockedSpctMap.put(spctId, spct);

            if (spct.getSoLuong() == null || spct.getSoLuong() < item.getSoLuong()) {
                return "redirect:/cart?outOfStock";
            }

            BigDecimal giaBan = spct.getGiaBan() != null ? spct.getGiaBan() : BigDecimal.ZERO;
            tongTienGoc = tongTienGoc.add(giaBan.multiply(BigDecimal.valueOf(item.getSoLuong())));
        }

        CouponResult couponResult = validateCoupon(request.getMaKhuyenMai(), tongTienGoc);
        BigDecimal tienGiam = couponResult.getTienGiam();
        BigDecimal tongTienSauGiam = tongTienGoc.subtract(tienGiam);

        if ("applyCoupon".equals(action)) {
            return renderCheckoutPage(
                    model,
                    khachHang,
                    selectedItems,
                    request,
                    couponResult.getCouponError(),
                    couponResult.isCouponSuccess(),
                    tongTienGoc,
                    tienGiam,
                    request.getPhiVanChuyen(),
                    tongTienSauGiam.add(request.getPhiVanChuyen() != null ? request.getPhiVanChuyen() : BigDecimal.ZERO)
            );
        }

        if (couponResult.getCouponError() != null) {
            return renderCheckoutPage(
                    model,
                    khachHang,
                    selectedItems,
                    request,
                    couponResult.getCouponError(),
                    false,
                    tongTienGoc,
                    BigDecimal.ZERO,
                    request.getPhiVanChuyen(),
                    tongTienGoc.add(request.getPhiVanChuyen() != null ? request.getPhiVanChuyen() : BigDecimal.ZERO)
            );
        }

        if (request.getPhuongThucThanhToan() == null
                || (!"COD".equals(request.getPhuongThucThanhToan())
                && !"VNPAY".equals(request.getPhuongThucThanhToan()))) {
            return renderCheckoutPage(
                    model,
                    khachHang,
                    selectedItems,
                    request,
                    null,
                    false,
                    tongTienGoc,
                    tienGiam,
                    request.getPhiVanChuyen(),
                    tongTienSauGiam.add(request.getPhiVanChuyen() != null ? request.getPhiVanChuyen() : BigDecimal.ZERO)
            );
        }

        if (isBlank(request.getTenNguoiNhan())
                || isBlank(request.getSoDienThoai())
                || isBlank(request.getDiaChiGiaoHang())
                || isBlank(request.getTinhThanh())
                || isBlank(request.getPhuongXa())) {
            return renderCheckoutPage(
                    model,
                    khachHang,
                    selectedItems,
                    request,
                    null,
                    false,
                    tongTienGoc,
                    tienGiam,
                    request.getPhiVanChuyen(),
                    tongTienSauGiam.add(request.getPhiVanChuyen() != null ? request.getPhiVanChuyen() : BigDecimal.ZERO)
            );
        }

        KhuyenMai khuyenMai = couponResult.getKhuyenMai();

        String diaChiFull = String.join(", ",
                safe(request.getDiaChiGiaoHang()),
                safe(request.getPhuongXa()),

                safe(request.getTinhThanh())
        );

        BigDecimal phiShip;
        try {
            GoongShippingService.ShippingResult shipResult =
                    goongShippingService.calculateByProvince(request.getTinhThanh());
            phiShip = shipResult.getPhiVanChuyen();
        } catch (Exception e) {
            return renderCheckoutPage(
                    model,
                    khachHang,
                    selectedItems,
                    request,
                    "shipping_error",
                    false,
                    tongTienGoc,
                    tienGiam,
                    BigDecimal.ZERO,
                    tongTienSauGiam
            );
        }

        if (request.getPhiVanChuyen() != null) {
            BigDecimal chenhlech = request.getPhiVanChuyen().subtract(phiShip).abs();
            if (chenhlech.compareTo(BigDecimal.ZERO) > 0) {
                return renderCheckoutPage(
                        model,
                        khachHang,
                        selectedItems,
                        request,
                        "invalid_shipping_fee",
                        false,
                        tongTienGoc,
                        tienGiam,
                        phiShip,
                        tongTienSauGiam.add(phiShip)
                );
            }
        }

        BigDecimal tongThanhToan = tongTienSauGiam.add(phiShip);

        DonHang donHang = new DonHang();
        donHang.setMaDonHang("DH" + System.currentTimeMillis());
        donHang.setKhachHang(khachHang);
        donHang.setTenNguoiNhan(request.getTenNguoiNhan());
        donHang.setSoDienThoai(request.getSoDienThoai());
        donHang.setDiaChiGiaoHang(diaChiFull);
        donHang.setGhiChu(request.getGhiChu());
        donHang.setPhuongThucThanhToan(request.getPhuongThucThanhToan());
        donHang.setKhuyenMai(khuyenMai);
        donHang.setTienGiam(tienGiam);
        donHang.setPhiVanChuyen(phiShip);
        donHang.setTongTien(tongThanhToan);
        donHang.setTrangThaiId(1);
        donHang.setLoaiDon(0);
        donHang.setNgayTao(LocalDateTime.now());

        if ("COD".equals(request.getPhuongThucThanhToan())) {
            donHang.setTrangThaiThanhToan("CHUA_THANH_TOAN");
        } else if ("VNPAY".equals(request.getPhuongThucThanhToan())) {
            donHang.setTrangThaiThanhToan("CHO_THANH_TOAN");
        }

        for (GioHangChiTiet item : selectedItems) {
            SanPhamChiTiet spct = lockedSpctMap.get(item.getSanPhamChiTiet().getId());

            DonHangChiTiet ct = new DonHangChiTiet();
            ct.setDonHang(donHang);
            ct.setSanPhamChiTiet(spct);
            ct.setSoLuong(item.getSoLuong());
            ct.setGiaTaiThoiDiem(spct.getGiaBan());

            chiTietDonHangList.add(ct);
        }

        donHang.setChiTietList(chiTietDonHangList);
        donHangRepository.save(donHang);

        if (khuyenMai != null && "COD".equals(request.getPhuongThucThanhToan())) {
            khuyenMai.setSoLuong(khuyenMai.getSoLuong() - 1);
            khuyenMaiClientRepository.save(khuyenMai);
        }

        clearCheckoutSession(session);

        if ("VNPAY".equals(request.getPhuongThucThanhToan())) {
            return "redirect:/payment/create?amount=" + tongThanhToan.longValue()
                    + "&orderCode=" + donHang.getMaDonHang();
        }

        Integer gioHangId = gioHang.getId();
        List<Integer> selectedItemIds = selectedItems.stream()
                .map(GioHangChiTiet::getId)
                .collect(Collectors.toList());

        gioHangChiTietRepository.deleteSelectedItems(gioHangId, selectedItemIds);
        gioHangChiTietRepository.flush();

        return "redirect:/order/success/" + donHang.getId();
    }

    @GetMapping("/success/{id}")
    public String success(@PathVariable Integer id,
                          Authentication authentication,
                          Model model) {
        KhachHang khachHang = getCurrentKhachHang(authentication);

        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n hÃ ng"));

        if (!donHang.getKhachHang().getId().equals(khachHang.getId())) {
            return "redirect:/order/my-orders";
        }

        BigDecimal tongTienSanPham = tinhTongTienDonHang(donHang);
        BigDecimal phiVanChuyen = donHang.getPhiVanChuyen() != null ? donHang.getPhiVanChuyen() : BigDecimal.ZERO;

        model.addAttribute("order", donHang);
        model.addAttribute("tongTienSanPham", tongTienSanPham);
        model.addAttribute("phiVanChuyen", phiVanChuyen);
        model.addAttribute("activeMenu", "orders");
        return "client/order-success";
    }

    @GetMapping("/my-orders")
    public String myOrders(Authentication authentication, Model model) {
        KhachHang khachHang = getCurrentKhachHang(authentication);

        List<DonHang> allOrders =
                donHangRepository.findByKhachHang_IdOrderByNgayTaoDesc(khachHang.getId());

        List<DonHang> completedOrders = allOrders.stream()
                .filter(o ->
                        ("COD".equals(o.getPhuongThucThanhToan()) && o.getTrangThaiId() != 4)
                                || "DA_THANH_TOAN".equals(o.getTrangThaiThanhToan())
                )
                .collect(Collectors.toList());

        List<DonHang> pendingOrders = allOrders.stream()
                .filter(o -> !completedOrders.contains(o))
                .collect(Collectors.toList());

        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("activeMenu", "orders");

        return "client/my-orders";
    }

    @GetMapping("/detail/{id}")
    public String orderDetail(@PathVariable Integer id,
                              Authentication authentication,
                              Model model) {

        KhachHang khachHang = getCurrentKhachHang(authentication);

        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n hÃ ng"));

        if (!donHang.getKhachHang().getId().equals(khachHang.getId())) {
            return "redirect:/order/my-orders";
        }

        BigDecimal tongTienSanPham = tinhTongTienDonHang(donHang);
        BigDecimal phiVanChuyen = donHang.getPhiVanChuyen() != null ? donHang.getPhiVanChuyen() : BigDecimal.ZERO;

        model.addAttribute("order", donHang);
        model.addAttribute("orderDetails", donHang.getChiTietList());
        model.addAttribute("tongTienSanPham", tongTienSanPham);
        model.addAttribute("phiVanChuyen", phiVanChuyen);
        model.addAttribute("activeMenu", "orders");

        return "client/order-detail";
    }

    @PostMapping("/cancel/{id}")
    @Transactional
    public String cancelOrder(@PathVariable Integer id,
                              Authentication authentication) {

        KhachHang khachHang = getCurrentKhachHang(authentication);

        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n"));

        if (!donHang.getKhachHang().getId().equals(khachHang.getId())) {
            return "redirect:/order/my-orders";
        }

        if (donHang.getTrangThaiId() != 1) {
            return "redirect:/order/my-orders";
        }

        if ("DA_THANH_TOAN".equals(donHang.getTrangThaiThanhToan())) {
            for (DonHangChiTiet ct : donHang.getChiTietList()) {
                SanPhamChiTiet spct = sanPhamChiTietRepository.findByIdForUpdate(ct.getSanPhamChiTiet().getId())
                        .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m"));

                spct.setSoLuong(spct.getSoLuong() + ct.getSoLuong());
                sanPhamChiTietRepository.save(spct);
            }
        }

        donHang.setTrangThaiId(4);
        donHang.setTrangThaiThanhToan("DA_HUY");
        donHangRepository.save(donHang);

        return "redirect:/order/my-orders";
    }

    private String renderCheckoutPage(Model model,
                                      KhachHang khachHang,
                                      List<GioHangChiTiet> items,
                                      CheckoutRequest request,
                                      String couponError,
                                      boolean couponSuccess,
                                      BigDecimal tongTienGoc,
                                      BigDecimal tienGiam,
                                      BigDecimal phiVanChuyen,
                                      BigDecimal tongThanhToan) {

        BigDecimal tongMacDinh = tinhTongTien(items);

        model.addAttribute("checkoutItems", items);
        model.addAttribute("khachHang", khachHang);

        model.addAttribute("selectedIds",
                request != null ? request.getSelectedIds() : items.stream().map(GioHangChiTiet::getId).toList());

        model.addAttribute("maKhuyenMai", request != null ? request.getMaKhuyenMai() : null);
        model.addAttribute("tenNguoiNhan", request != null ? request.getTenNguoiNhan() : null);
        model.addAttribute("soDienThoai", request != null ? request.getSoDienThoai() : null);
        model.addAttribute("diaChiGiaoHang", request != null ? request.getDiaChiGiaoHang() : null);
        model.addAttribute("ghiChu", request != null ? request.getGhiChu() : null);
        model.addAttribute("phuongThucThanhToan", request != null ? request.getPhuongThucThanhToan() : null);

        model.addAttribute("tinhThanh", request != null ? request.getTinhThanh() : null);
        model.addAttribute("phuongXa", request != null ? request.getPhuongXa() : null);

        model.addAttribute("provinceCode", request != null ? request.getProvinceCode() : null);
        model.addAttribute("wardCode", request != null ? request.getWardCode() : null);

        model.addAttribute("couponError", couponError);
        model.addAttribute("couponSuccess", couponSuccess);

        model.addAttribute("tongTien", tongMacDinh);
        model.addAttribute("tongTienGoc", tongTienGoc != null ? tongTienGoc : tongMacDinh);
        model.addAttribute("tienGiam", tienGiam != null ? tienGiam : BigDecimal.ZERO);
        model.addAttribute("phiVanChuyen", phiVanChuyen != null ? phiVanChuyen : BigDecimal.ZERO);
        model.addAttribute("tongThanhToan", tongThanhToan != null ? tongThanhToan : tongMacDinh);

        String selectedCode = request != null ? request.getMaKhuyenMai() : null;
        List<Map<String, Object>> voucherList =
                buildVoucherList(tongTienGoc != null ? tongTienGoc : tongMacDinh, selectedCode);

        model.addAttribute("voucherList", voucherList);

        return "client/checkout";
    }
}