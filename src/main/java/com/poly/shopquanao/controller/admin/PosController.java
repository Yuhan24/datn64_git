package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.DonHang;
import com.poly.shopquanao.entity.DonHangChiTiet;
import com.poly.shopquanao.entity.KhachHang;
import com.poly.shopquanao.entity.KhuyenMai;
import com.poly.shopquanao.entity.SanPhamChiTiet;
import com.poly.shopquanao.repository.admin.DonHangADRepository;
import com.poly.shopquanao.repository.admin.DonHangChiTietADRepository;
import com.poly.shopquanao.repository.admin.KhachHangRepository;
import com.poly.shopquanao.repository.admin.KhuyenMaiADRepository;
import com.poly.shopquanao.repository.admin.KichCoRepository;
import com.poly.shopquanao.repository.admin.MauSacRepository;
import com.poly.shopquanao.repository.admin.NhanVienRepository;
import com.poly.shopquanao.repository.admin.SanPhamChiTietADMRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.Map;
@Controller
@RequestMapping("/admin/pos")

public class PosController {

    @Autowired
    private    SanPhamChiTietADMRepository sanPhamChiTietADMRepository;
    @Autowired
    private DonHangADRepository donHangADRepository;

    @Autowired
    private DonHangChiTietADRepository donHangChiTietADRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private MauSacRepository mauSacRepository;

    @Autowired
    private KichCoRepository kichCoRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhuyenMaiADRepository khuyenMaiADRepository;


    @GetMapping("")
    public String pos(@RequestParam(required = false) String keyword,
                      @RequestParam(required = false) Integer mauSacId,
                      @RequestParam(required = false) Integer kichCoId,
                      @RequestParam(required = false) BigDecimal giaMin,
                      @RequestParam(required = false) BigDecimal giaMax,
                      @RequestParam(required = false) Integer donHangId ,Model model ) {

        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        //Chỉ lấy đơn hàng tại quầy 1 = off 0 = onl
        List<DonHang> hoaDonCHo = donHangADRepository.findByLoaiDonAndTrangThaiIdOrderByNgayTaoDesc(1,1);

        //chỉ lấy sản phẩm chi tiết đang hoạt động
//        List<SanPhamChiTiet> listSpct = sanPhamChiTietADMRepository.findByTrangThaiTrue();

        List<SanPhamChiTiet> listSpct = sanPhamChiTietADMRepository.searchForPos(
                keyword, mauSacId, kichCoId, giaMin, giaMax
        );



        model.addAttribute("pageTitle", "Bán hàng tại quầy");
        model.addAttribute("activeMenu", "pos");
        model.addAttribute("content", "admin/pos :: content");

        model.addAttribute("hoaDonCho",hoaDonCHo);
        model.addAttribute("listSpct",listSpct);
        model.addAttribute("selectedDonHangId" ,donHangId);

        model.addAttribute("keyword", keyword);
        model.addAttribute("mauSacId", mauSacId);
        model.addAttribute("kichCoId", kichCoId);
        model.addAttribute("giaMin", giaMin);
        model.addAttribute("giaMax", giaMax);

        model.addAttribute("now", java.time.LocalDateTime.now());

        model.addAttribute("listMauSac", mauSacRepository.findByTrangThaiTrue());
        model.addAttribute("listKichCo", kichCoRepository.findByTrangThaiTrue());

        if (donHangId != null) {
            DonHang donHang = donHangADRepository.findById(donHangId).orElse(null);

            if (donHang == null) {
                model.addAttribute("error", "Hóa đơn này đã hết hạn hoặc đã bị xóa");
            } else {
                List<DonHangChiTiet> gioHang = donHangChiTietADRepository.findByDonHang_Id(donHangId);
                model.addAttribute("donHang", donHang);
                model.addAttribute("gioHang", gioHang);
                model.addAttribute("selectedCustomer", donHang.getKhachHang());
            }
        }


        return "admin/layout";
    }

    @PostMapping("/create-order")
    public String createOrder(Authentication authentication, RedirectAttributes ra){
        // 🔴 Đếm số hóa đơn chờ hiện tại
        List<DonHang> hoaDonCho = donHangADRepository
                .findByLoaiDonAndTrangThaiIdOrderByNgayTaoDesc(1,1);

        if (hoaDonCho.size() >= 10) {
            ra.addFlashAttribute("error", "Chỉ được tạo tối đa 10 hóa đơn chờ");
            return "redirect:/admin/pos";
        }
        DonHang dh = new DonHang();
        dh.setMaDonHang("HD"+ System.currentTimeMillis());
        dh.setNgayTao(LocalDateTime.now());
        dh.setTongTien(BigDecimal.ZERO);


        //trạng thái đơn
        dh.setLoaiDon(1);
        dh.setTrangThaiId(1); // chờ thanh toán
        dh.setTrangThaiThanhToan("CHUA_THANH_TOAN");

        if (authentication != null) {
            String username = authentication.getName();
            nhanVienRepository.findByTenDangNhap(username)
                    .ifPresent(dh::setNhanVien);
        }
        donHangADRepository.save(dh);

        ra.addFlashAttribute("success" ,"Đã tạo hóa đơn mới");
        return "redirect:/admin/pos?donHangId=" + dh.getId() ;

    }


    @PostMapping("/update-qty")
    @Transactional
    public String updateQty(
            @RequestParam Integer donHangChiTietId,
            @RequestParam Integer soLuong,
            RedirectAttributes ra
    ){
        DonHangChiTiet ct = donHangChiTietADRepository.findById(donHangChiTietId).orElseThrow();
        Integer donHangId = ct.getDonHang().getId();

        SanPhamChiTiet spct = sanPhamChiTietADMRepository
                .findById(ct.getSanPhamChiTiet().getId())
                .orElseThrow();

        int soLuongCu = ct.getSoLuong();

        if (soLuong <= 0){
            spct.setSoLuong((spct.getSoLuong() == null ? 0 : spct.getSoLuong()) + soLuongCu);
            sanPhamChiTietADMRepository.save(spct);

            donHangChiTietADRepository.delete(ct);
            updateTongTien(donHangId);
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        if (soLuong > soLuongCu) {
            int canThem = soLuong - soLuongCu;
            if (spct.getSoLuong() == null || spct.getSoLuong() < canThem) {
                ra.addFlashAttribute("error", "Số lượng vượt quá tồn kho");
                return "redirect:/admin/pos?donHangId=" + donHangId;
            }

            spct.setSoLuong(spct.getSoLuong() - canThem);
        } else if (soLuong < soLuongCu) {
            int traLai = soLuongCu - soLuong;
            spct.setSoLuong((spct.getSoLuong() == null ? 0 : spct.getSoLuong()) + traLai);
        }

        ct.setSoLuong(soLuong);
        donHangChiTietADRepository.save(ct);
        sanPhamChiTietADMRepository.save(spct);

        updateTongTien(donHangId);
        return "redirect:/admin/pos?donHangId=" + donHangId;
    }

    @PostMapping("/add-product")
    @Transactional
    public String addProduct(
            @RequestParam Integer donHangId,
            @RequestParam Integer spctId,
            RedirectAttributes ra
    ){
        DonHang donHang = donHangADRepository.findById(donHangId).orElseThrow();

        if (isExpiredOrder(donHang)) {
            ra.addFlashAttribute("error", "Hóa đơn đã quá 1 tiếng  và không còn hiệu lực");
            return "redirect:/admin/pos";
        }

        SanPhamChiTiet spct = sanPhamChiTietADMRepository.findById(spctId).orElseThrow();

        if (!Boolean.TRUE.equals(spct.getTrangThai())){
            ra.addFlashAttribute("error", "Sản phẩm chi tiết đang ngừng hoạt động");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        if (spct.getSoLuong() == null || spct.getSoLuong() <= 0){
            ra.addFlashAttribute("error", "Sản phẩm đã hết hàng");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        DonHangChiTiet ct = donHangChiTietADRepository
                .findByDonHang_IdAndSanPhamChiTiet_Id(donHangId, spctId)
                .orElse(null);

        // TRỪ KHO LUÔN KHI THÊM VÀO GIỎ
        spct.setSoLuong(spct.getSoLuong() - 1);
        sanPhamChiTietADMRepository.save(spct);

        if (ct != null){
            ct.setSoLuong(ct.getSoLuong() + 1);
            ct.setGiaTaiThoiDiem(spct.getGiaBan());
            donHangChiTietADRepository.save(ct);
        } else {
            DonHangChiTiet newCt = new DonHangChiTiet();
            newCt.setDonHang(donHang);
            newCt.setSanPhamChiTiet(spct);
            newCt.setSoLuong(1);
            newCt.setGiaTaiThoiDiem(spct.getGiaBan());
            donHangChiTietADRepository.save(newCt);
        }

        updateTongTien(donHangId);
        return "redirect:/admin/pos?donHangId=" + donHangId;
    }

    @PostMapping("/remove-item/{id}")
    @Transactional
    public String removeItem(@PathVariable Integer id){
        DonHangChiTiet dhct = donHangChiTietADRepository.findById(id).orElseThrow();
        Integer donHangId = dhct.getDonHang().getId();

        SanPhamChiTiet spct = sanPhamChiTietADMRepository
                .findById(dhct.getSanPhamChiTiet().getId())
                .orElse(null);

        if (spct != null) {
            int soLuongTraLai = dhct.getSoLuong() == null ? 0 : dhct.getSoLuong();
            spct.setSoLuong((spct.getSoLuong() == null ? 0 : spct.getSoLuong()) + soLuongTraLai);
            sanPhamChiTietADMRepository.save(spct);
        }

        donHangChiTietADRepository.delete(dhct);
        updateTongTien(donHangId);
        return "redirect:/admin/pos?donHangId=" + donHangId;
    }


    @PostMapping("/pay")
    @Transactional
    public String pay(
            @RequestParam Integer donHangId,
            @RequestParam(required = false) String phuongThucThanhToan,
            Authentication authentication,
            @RequestParam(required = false) BigDecimal tienKhachDua,
            RedirectAttributes ra
    ) {
        DonHang donHang = donHangADRepository.findById(donHangId).orElse(null);
        if (donHang == null) {
            ra.addFlashAttribute("error", "Hóa đơn không tồn tại");
            return "redirect:/admin/pos";
        }

        List<DonHangChiTiet> gioHang = donHangChiTietADRepository.findByDonHang_Id(donHangId);
        if (gioHang == null || gioHang.isEmpty()) {
            ra.addFlashAttribute("error", "Hóa đơn chưa có sản phẩm");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }
        if (isExpiredOrder(donHang)) {
            ra.addFlashAttribute("error", "Hóa đơn đã quá 1 tiếng  nên không thể thanh toán");
            return "redirect:/admin/pos";
        }

        BigDecimal tongTien = donHang.getTongTien() != null ? donHang.getTongTien() : BigDecimal.ZERO;

        if (phuongThucThanhToan == null || phuongThucThanhToan.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng chọn phương thức thanh toán");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        if ("TIEN_MAT".equals(phuongThucThanhToan)) {
            if (tienKhachDua == null) {
                ra.addFlashAttribute("error", "Vui lòng nhập tiền khách đưa");
                return "redirect:/admin/pos?donHangId=" + donHangId;
            }

            if (tienKhachDua.compareTo(tongTien) < 0) {
                ra.addFlashAttribute("error", "Tiền khách đưa phải lớn hơn hoặc bằng số tiền phải trả");
                return "redirect:/admin/pos?donHangId=" + donHangId;
            }
        }

        // Kiểm tra lại tồn kho bằng dữ liệu mới nhất từ DB
        for (DonHangChiTiet ct : gioHang) {
            SanPhamChiTiet spct = sanPhamChiTietADMRepository
                    .findById(ct.getSanPhamChiTiet().getId())
                    .orElse(null);

            if (spct == null) {
                ra.addFlashAttribute("error", "Có sản phẩm không tồn tại trong hệ thống");
                return "redirect:/admin/pos?donHangId=" + donHangId;
            }

            if (!Boolean.TRUE.equals(spct.getTrangThai())) {
                ra.addFlashAttribute("error",
                        "Sản phẩm " + spct.getSanPham().getTenSanPham() + " đang ngừng hoạt động");
                return "redirect:/admin/pos?donHangId=" + donHangId;
            }

            if (spct.getSoLuong() == null || ct.getSoLuong() > spct.getSoLuong()) {
                ra.addFlashAttribute("error",
                        "Sản phẩm " + spct.getSanPham().getTenSanPham()
                                + " - " + spct.getMauSac().getTenMau()
                                + " - size " + spct.getKichCo().getTenKichCo()
                                + " không đủ số lượng trong kho");
                return "redirect:/admin/pos?donHangId=" + donHangId;
            }
        }



        // Gán nhân viên đăng nhập
        if (authentication != null) {
            String username = authentication.getName();
            nhanVienRepository.findByTenDangNhap(username)
                    .ifPresent(donHang::setNhanVien);
        }

        // Trừ lượt voucher nếu có
        if (donHang.getKhuyenMai() != null) {
            KhuyenMai km = khuyenMaiADRepository.findById(donHang.getKhuyenMai().getId()).orElse(null);
            if (km != null && km.getSoLuong() != null && km.getSoLuong() > 0) {
                km.setSoLuong(km.getSoLuong() - 1);
                khuyenMaiADRepository.save(km);
            }
        }

        donHang.setPhuongThucThanhToan(phuongThucThanhToan);
        donHang.setTrangThaiId(3);
        donHang.setTrangThaiThanhToan("DA_THANH_TOAN");
        donHangADRepository.save(donHang);

        ra.addFlashAttribute("success", "Thanh toán thành công");
        return "redirect:/admin/pos";
    }


    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam Integer donHangId,
                               @RequestParam String maKhuyenMai,
                               RedirectAttributes ra) {

        DonHang donHang = donHangADRepository.findById(donHangId).orElseThrow();
        List<DonHangChiTiet> gioHang = donHangChiTietADRepository.findByDonHang_Id(donHangId);

        if (gioHang.isEmpty()) {
            ra.addFlashAttribute("error", "Hóa đơn chưa có sản phẩm, không thể áp voucher");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        if (maKhuyenMai == null || maKhuyenMai.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng nhập mã khuyến mãi");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }
        if (isExpiredOrder(donHang)) {
            ra.addFlashAttribute("error", "Hóa đơn đã quá 1 tiếng  nên không thể áp dụng khuyến mãi");
            return "redirect:/admin/pos";
        }

        KhuyenMai km = khuyenMaiADRepository.findByMaKhuyenMaiIgnoreCase(maKhuyenMai.trim())
                .orElse(null);

        if (km == null) {
            ra.addFlashAttribute("error", "Mã khuyến mãi không tồn tại");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        if (!Boolean.TRUE.equals(km.getTrangThai())) {
            ra.addFlashAttribute("error", "Khuyến mãi đang ngừng hoạt động");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        if (km.getSoLuong() == null || km.getSoLuong() <= 0) {
            ra.addFlashAttribute("error", "Khuyến mãi đã hết lượt sử dụng");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        LocalDateTime now = LocalDateTime.now();

        if (km.getNgayBatDau() != null && now.isBefore(km.getNgayBatDau())) {
            ra.addFlashAttribute("error", "Khuyến mãi chưa đến thời gian áp dụng");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        if (km.getNgayKetThuc() != null && now.isAfter(km.getNgayKetThuc())) {
            ra.addFlashAttribute("error", "Khuyến mãi đã hết hạn");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        BigDecimal tongTienHang = gioHang.stream()
                .map(ct -> ct.getGiaTaiThoiDiem().multiply(BigDecimal.valueOf(ct.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (km.getGiaTriDonToiThieu() != null
                && tongTienHang.compareTo(km.getGiaTriDonToiThieu()) < 0) {
            ra.addFlashAttribute("error", "Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        BigDecimal tienGiam;
        if (Boolean.TRUE.equals(km.getLoaiGiam())) {
            tienGiam = tongTienHang.multiply(km.getGiaTriGiam())
                    .divide(BigDecimal.valueOf(100));
        } else {
            tienGiam = km.getGiaTriGiam();
        }

        if (tienGiam.compareTo(tongTienHang) > 0) {
            tienGiam = tongTienHang;
        }

        donHang.setKhuyenMai(km);
        donHang.setTienGiam(tienGiam);
        donHang.setTongTien(tongTienHang.subtract(tienGiam));
        donHangADRepository.save(donHang);

        ra.addFlashAttribute("success", "Áp dụng khuyến mãi thành công");
        return "redirect:/admin/pos?donHangId=" + donHangId;
    }

    @GetMapping("/find-customer")
    public String findCustomer(
            @RequestParam(required = false) Integer donHangId,
            @RequestParam(required = false) String keyword,
            RedirectAttributes ra
    ) {
        if (donHangId == null) {
            ra.addFlashAttribute("error", "Vui lòng chọn hoặc tạo hóa đơn trước khi thêm khách hàng");
            return "redirect:/admin/pos";
        }


        DonHang donHang = donHangADRepository.findById(donHangId).orElse(null);
        if (donHang == null) {
            ra.addFlashAttribute("error", "Hóa đơn không tồn tại");
            return "redirect:/admin/pos";
        }
        if (isExpiredOrder(donHang)) {
            ra.addFlashAttribute("error", "Hóa đơn đã quá 1 tiếng  và không còn hiệu lực");
            return "redirect:/admin/pos";
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng nhập số điện thoại");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        KhachHang kh = khachHangRepository.findBySoDienThoai(keyword.trim()).orElse(null);
        if (kh == null) {
            ra.addFlashAttribute("error", "Không tìm thấy khách hàng");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        donHang.setKhachHang(kh);
        donHang.setSoDienThoai(kh.getSoDienThoai());
        donHang.setTenNguoiNhan(kh.getHoTen());
        donHangADRepository.save(donHang);

        ra.addFlashAttribute("success", "Đã chọn khách hàng: " + kh.getHoTen());
        return "redirect:/admin/pos?donHangId=" + donHangId;
    }

    @PostMapping("/select-guest")
    public String selectGuest(
            @RequestParam(required = false) Integer donHangId,
            RedirectAttributes ra
    ) {
        if (donHangId == null) {
            ra.addFlashAttribute("error", "Vui lòng chọn hoặc tạo hóa đơn trước khi thêm khách hàng");
            return "redirect:/admin/pos";
        }

        DonHang dh = donHangADRepository.findById(donHangId).orElse(null);
        if (dh == null) {
            ra.addFlashAttribute("error", "Hóa đơn không tồn tại");
            return "redirect:/admin/pos";
        }
        if (isExpiredOrder(dh)) {
            ra.addFlashAttribute("error", "Hóa đơn đã quá 1 tiếng và không còn hiệu lực");
            return "redirect:/admin/pos";
        }

        dh.setKhachHang(null);
        dh.setTenNguoiNhan("Khách vãng lai");
        dh.setSoDienThoai(null);
        donHangADRepository.save(dh);

        ra.addFlashAttribute("success", "Đã chọn khách vãng lai");
        return "redirect:/admin/pos?donHangId=" + donHangId;
    }


    private void updateTongTien(Integer donHangId) {
        DonHang donHang = donHangADRepository.findById(donHangId).orElseThrow();
        List<DonHangChiTiet> gioHang = donHangChiTietADRepository.findByDonHang_Id(donHangId);
        BigDecimal tongHang =gioHang.stream()
                .map(x->x.getGiaTaiThoiDiem().multiply(BigDecimal.valueOf(x.getSoLuong())))
                .reduce(BigDecimal.ZERO,BigDecimal :: add);

        BigDecimal tienGiam =donHang.getTienGiam() != null ? donHang.getTienGiam() : BigDecimal.ZERO;
        //nếu tổng hàng giảm xuống thấp hơn số tiền giảm thì chănj lại
        if (tienGiam.compareTo(tongHang) >0){
            tienGiam = tongHang;
            donHang.setTienGiam(tienGiam);

        }
        donHang.setTongTien(tongHang.subtract(tienGiam));
        donHangADRepository.save(donHang);

    }


    @PostMapping("/delete-order/{id}")
    @Transactional
    public String deleteOrder(@PathVariable Integer id, RedirectAttributes ra) {
        DonHang donHang = donHangADRepository.findById(id).orElse(null);

        if (donHang == null) {
            ra.addFlashAttribute("error", "Hóa đơn không tồn tại");
            return "redirect:/admin/pos";
        }

        if (donHang.getLoaiDon() == null || donHang.getLoaiDon() != 1) {
            ra.addFlashAttribute("error", "Chỉ được xóa hóa đơn tại quầy");
            return "redirect:/admin/pos";
        }

        if (donHang.getTrangThaiId() == null || donHang.getTrangThaiId() != 1) {
            ra.addFlashAttribute("error", "Chỉ được xóa hóa đơn đang chờ thanh toán");
            return "redirect:/admin/pos?donHangId=" + id;
        }

        if ("DA_THANH_TOAN".equalsIgnoreCase(donHang.getTrangThaiThanhToan())) {
            ra.addFlashAttribute("error", "Không thể xóa hóa đơn đã thanh toán");
            return "redirect:/admin/pos?donHangId=" + id;
        }

        List<DonHangChiTiet> chiTietList = donHangChiTietADRepository.findByDonHang_Id(id);

        for (DonHangChiTiet ct : chiTietList) {
            SanPhamChiTiet spct = sanPhamChiTietADMRepository
                    .findById(ct.getSanPhamChiTiet().getId())
                    .orElse(null);

            if (spct != null) {
                int soLuongTra = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
                spct.setSoLuong((spct.getSoLuong() == null ? 0 : spct.getSoLuong()) + soLuongTra);
                sanPhamChiTietADMRepository.save(spct);
            }
        }

        donHangChiTietADRepository.deleteAll(chiTietList);
        donHangADRepository.delete(donHang);

        ra.addFlashAttribute("success", "Đã xóa hóa đơn thành công");
        return "redirect:/admin/pos";
    }



    private boolean isExpiredOrder(DonHang donHang) {
        return donHang != null
                && donHang.getNgayTao() != null
                && donHang.getTrangThaiId() != null
                && donHang.getTrangThaiId() == 1
                && "CHUA_THANH_TOAN".equalsIgnoreCase(donHang.getTrangThaiThanhToan())
                && donHang.getNgayTao().isBefore(LocalDateTime.now().minusHours(1));
    }

    @PostMapping("/decrease-quantity/{id}")
    @Transactional
    public String decreaseQuantity(@PathVariable Integer id, RedirectAttributes ra) {
        DonHangChiTiet ct = donHangChiTietADRepository.findById(id).orElse(null);

        if (ct == null) {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm trong giỏ");
            return "redirect:/admin/pos";
        }

        Integer donHangId = ct.getDonHang().getId();
        SanPhamChiTiet spct = sanPhamChiTietADMRepository
                .findById(ct.getSanPhamChiTiet().getId())
                .orElse(null);

        if (spct == null) {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm chi tiết");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        if (ct.getSoLuong() <= 1){
            ra.addFlashAttribute("error", "Đã đạt số lượng nhỏ nhất");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        ct.setSoLuong(ct.getSoLuong() - 1);
        spct.setSoLuong((spct.getSoLuong() == null ? 0 : spct.getSoLuong()) + 1);

        donHangChiTietADRepository.save(ct);
        sanPhamChiTietADMRepository.save(spct);

        updateTongTienDonHang(ct.getDonHang());

        ra.addFlashAttribute("success", "Đã giảm số lượng");
        return "redirect:/admin/pos?donHangId=" + donHangId;
    }

    @PostMapping("/increase-quantity/{id}")
    @Transactional
    public String increaseQuantity(@PathVariable Integer id, RedirectAttributes ra) {
        DonHangChiTiet ct = donHangChiTietADRepository.findById(id).orElse(null);

        if (ct == null) {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm trong giỏ");
            return "redirect:/admin/pos";
        }

        SanPhamChiTiet spct = sanPhamChiTietADMRepository
                .findById(ct.getSanPhamChiTiet().getId())
                .orElse(null);

        if (spct == null) {
            ra.addFlashAttribute("error", "Không tìm thấy sản phẩm chi tiết");
            return "redirect:/admin/pos?donHangId=" + ct.getDonHang().getId();
        }

        if (!Boolean.TRUE.equals(spct.getTrangThai())) {
            ra.addFlashAttribute("error", "Sản phẩm đang ngừng hoạt động");
            return "redirect:/admin/pos?donHangId=" + ct.getDonHang().getId();
        }

        if (spct.getSoLuong() == null || spct.getSoLuong() <= 0) {
            ra.addFlashAttribute("error", "Số lượng vượt quá tồn kho");
            return "redirect:/admin/pos?donHangId=" + ct.getDonHang().getId();
        }

        ct.setSoLuong(ct.getSoLuong() + 1);
        spct.setSoLuong(spct.getSoLuong() - 1);

        donHangChiTietADRepository.save(ct);
        sanPhamChiTietADMRepository.save(spct);

        updateTongTienDonHang(ct.getDonHang());

        ra.addFlashAttribute("success", "Đã tăng số lượng");
        return "redirect:/admin/pos?donHangId=" + ct.getDonHang().getId();
    }

    private void updateTongTienDonHang(DonHang donHang) {
        List<DonHangChiTiet> list = donHangChiTietADRepository.findByDonHang_Id(donHang.getId());

        java.math.BigDecimal tong = java.math.BigDecimal.ZERO;

        for (DonHangChiTiet item : list) {
            if (item.getThanhTien() != null) {
                tong = tong.add(item.getThanhTien());
            }
        }

        java.math.BigDecimal tienGiam = donHang.getTienGiam() != null ? donHang.getTienGiam() : java.math.BigDecimal.ZERO;
        donHang.setTongTien(tong.subtract(tienGiam));
        donHangADRepository.save(donHang);
    }
}
