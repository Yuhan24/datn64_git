package com.poly.shopquanao.controller.admin;

import com.poly.shopquanao.entity.DonHang;
import com.poly.shopquanao.entity.DonHangChiTiet;
import com.poly.shopquanao.entity.KhachHang;
import com.poly.shopquanao.entity.SanPhamChiTiet;
import com.poly.shopquanao.repository.admin.DonHangADRepository;
import com.poly.shopquanao.repository.admin.DonHangChiTietADRepository;
import com.poly.shopquanao.repository.admin.KhachHangRepository;
import com.poly.shopquanao.repository.admin.KichCoRepository;
import com.poly.shopquanao.repository.admin.MauSacRepository;
import com.poly.shopquanao.repository.admin.NhanVienRepository;
import com.poly.shopquanao.repository.admin.SanPhamChiTietADMRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
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

        if (donHangId != null){
            DonHang donHang = donHangADRepository.findById(donHangId).orElse(null);
            List<DonHangChiTiet> gioHang =donHangChiTietADRepository.findByDonHang_Id(donHangId);
            model.addAttribute("donHang" ,donHang);
            model.addAttribute("gioHang" , gioHang);
            model.addAttribute("selectedCustomer" ,donHang !=null ?donHang.getKhachHang() :null);
        }


        return "admin/layout";
    }

    @PostMapping("/create-order")
    public String createOrder(Authentication authentication, RedirectAttributes ra){
        DonHang dh = new DonHang();
        dh.setMaDonHang("HD"+ System.currentTimeMillis());
        dh.setNgayTao(LocalDateTime.now());
        dh.setTongTien(BigDecimal.ZERO);


        //trạng thái đơn
        dh.setLoaiDon(1);
        dh.setTrangThaiId(1); // chờ thanh toán ;

        if (authentication != null) {
            String username = authentication.getName();
            nhanVienRepository.findByTenDangNhap(username)
                    .ifPresent(dh::setNhanVien);
        }
        donHangADRepository.save(dh);

        ra.addFlashAttribute("success" ,"Đã tạo hóa đơn mới");
        return "redirect:/admin/pos?donHangId=" + dh.getId() ;

    }
        @PostMapping("/add-product")
        public String addProduct(
                @RequestParam Integer donHangId,
                @RequestParam Integer spctId,
                RedirectAttributes ra
        ){
            DonHang donHang = donHangADRepository.findById(donHangId).orElseThrow();
            SanPhamChiTiet spct =sanPhamChiTietADMRepository.findById(spctId).orElseThrow();
            if (!Boolean.TRUE.equals(spct.getTrangThai())){
                ra.addFlashAttribute("error", "Sản phẩm chi tiết đang ngừng hoạt động");
                return "redirect:/admin/pos?donHangId=" + donHangId;
            }
            if (spct.getSoLuong()==null||spct.getSoLuong()<=0){
                ra.addFlashAttribute("error", "Sản phẩm chi tiết đang ngừng hoạt động");
                return "redirect:/admin/pos?donHangId=" + donHangId;
            }
            DonHangChiTiet ct = donHangChiTietADRepository.findByDonHang_IdAndSanPhamChiTiet_Id(donHangId,spctId).orElse(null);
            if (ct != null){
                if (ct.getSoLuong() + 1 > spct.getSoLuong()){
                    ra.addFlashAttribute("error", "Không đủ số lượng trong kho");
                    return "redirect:/admin/pos?donHangId=" + donHangId;

                }
                ct.setSoLuong(ct.getSoLuong()+1);
                ct.setGiaTaiThoiDiem(spct.getGiaBan());
                donHangChiTietADRepository.save(ct);
            }else {
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

            @PostMapping("/update-qty")
            public String updateQty(
                    @RequestParam Integer donHangChiTietId,
                    @RequestParam Integer soLuong,
                    RedirectAttributes ra

            ){
        DonHangChiTiet ct = donHangChiTietADRepository.findById(donHangChiTietId).orElseThrow();
        Integer donHangId = ct.getDonHang().getId();
        SanPhamChiTiet spct = ct.getSanPhamChiTiet();

        if (soLuong<=0){
            donHangChiTietADRepository.delete(ct);
            updateTongTien(donHangId);
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }
        if (soLuong> spct.getSoLuong()){
            ra.addFlashAttribute("error", "Số lượng vượt quá tồn kho");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }
        ct.setSoLuong(soLuong);
        donHangChiTietADRepository.save(ct);
        updateTongTien(donHangId);
                return "redirect:/admin/pos?donHangId=" + donHangId;
            }

            @PostMapping("/remove-item/{id}")
            public  String removeItem(@PathVariable Integer id){
        DonHangChiTiet dhct = donHangChiTietADRepository.findById(id).orElseThrow();
        Integer donHangId =dhct.getDonHang().getId();
        donHangChiTietADRepository.delete(dhct);
        updateTongTien(donHangId);
                return "redirect:/admin/pos?donHangId=" + donHangId;
            }

            @PostMapping("/pay")
            public String pay(
                    @RequestParam Integer donHangId,
                    @RequestParam(required = false) String phuongThucThanhToan,
                    Authentication authentication,
                    @RequestParam BigDecimal tienKhachDua,
                    RedirectAttributes ra
            ){
        DonHang donHang = donHangADRepository.findById(donHangId).orElseThrow();
        List<DonHangChiTiet> gioHang = donHangChiTietADRepository.findByDonHang_Id(donHangId);
        if (gioHang.isEmpty()){
            ra.addFlashAttribute("error", "Hóa đơn chưa có sản phẩm");
            return "redirect:/admin/pos?donHangId=" + donHangId;
        }

        BigDecimal tongTien = donHang.getTongTien();
                // validate tiền khách đưa
                if (tienKhachDua.compareTo(tongTien) < 0) {
                    ra.addFlashAttribute("error", "Tiền khách đưa phải lớn hơn hoặc bằng số tiền phải trả");
                    return "redirect:/admin/pos?donHangId=" + donHangId;
                }

        for (DonHangChiTiet ct :gioHang){
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            if (ct.getSoLuong()>spct.getSoLuong()){
                ra.addFlashAttribute("error",
                        "Sản phẩm " + spct.getSanPham().getTenSanPham() + " không đủ số lượng");
                return "redirect:/admin/pos?donHangId=" + donHangId;
            }
        }
                //trừ kho khi thanh toán
                for (DonHangChiTiet ct : gioHang){
                    SanPhamChiTiet spct = ct.getSanPhamChiTiet();
                    spct.setSoLuong(spct.getSoLuong() - ct.getSoLuong());
                    sanPhamChiTietADMRepository.save(spct);
                }

                if (authentication != null) {
                    String username = authentication.getName();
                    nhanVienRepository.findByTenDangNhap(username)
                            .ifPresent(donHang::setNhanVien);
                }
                donHang.setTrangThaiId(3);//đã thanh toán
                donHang.setTrangThaiThanhToan("DA_THANH_TOAN");
                donHangADRepository.save(donHang);
                ra.addFlashAttribute("success", "Thanh toán thành công");
                return "redirect:/admin/pos";

            }

            @GetMapping("/find-customer")
            public String findCustomer(
                    @RequestParam Integer donHangId,
                    @RequestParam String keyword,
                    RedirectAttributes ra
            ){
                DonHang donHang = donHangADRepository.findById(donHangId).orElseThrow();
                if (keyword == null || keyword.trim().isEmpty()){
                    ra.addFlashAttribute("error", "Vui lòng nhập số điện thoại");
                    return "redirect:/admin/pos?donHangId=" + donHangId;
                }

                KhachHang kh = khachHangRepository.findBySoDienThoai(keyword.trim()).orElse(null);
                if (kh == null){
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
                    @RequestParam Integer donHangId,
                    RedirectAttributes ra
            ){
                    DonHang dh = donHangADRepository.findById(donHangId).orElseThrow();
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
        BigDecimal tong = gioHang.stream()
                .map(x ->x.getGiaTaiThoiDiem().multiply(BigDecimal.valueOf(x.getSoLuong())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        donHang.setTongTien(tong);
        donHangADRepository.save(donHang);
    }


}
