package com.poly.shopquanao.service.impl;

import com.poly.shopquanao.entity.GioHang;
import com.poly.shopquanao.entity.GioHangChiTiet;
import com.poly.shopquanao.entity.KhachHang;
import com.poly.shopquanao.repository.client.GioHangChiTietRepository;
import com.poly.shopquanao.repository.client.GioHangRepository;
import com.poly.shopquanao.repository.client.SanPhamChiTietRepository;
import com.poly.shopquanao.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final GioHangRepository gioHangRepo;
    private final GioHangChiTietRepository chiTietRepo;
    private final SanPhamChiTietRepository sanPhamChiTietRepo;



    @Override
    public void addToCart(Integer khachHangId, Integer sanPhamChiTietId, Integer soLuong) {

        if (khachHangId == null) throw new RuntimeException("Thiếu khách hàng");
        if (sanPhamChiTietId == null) throw new RuntimeException("Thiếu sản phẩm chi tiết");
        if (soLuong == null || soLuong <= 0) throw new RuntimeException("Số lượng không hợp lệ");

        GioHang gioHang = gioHangRepo.findByKhachHang_Id(khachHangId)
                .orElseGet(() -> gioHangRepo.save(
                        GioHang.builder()
                                .khachHang(KhachHang.builder().id(khachHangId).build())
                                .ngayTao(LocalDateTime.now())
                                .build()
                ));

        var spct = sanPhamChiTietRepo.findById(sanPhamChiTietId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        System.out.println("[ADD_CART] spctId=" + spct.getId()
                + " sp=" + spct.getSanPham().getTenSanPham()
                + " size=" + spct.getKichCo().getTenKichCo()
                + " mau=" + spct.getMauSac().getTenMau());
        GioHangChiTiet chiTiet = chiTietRepo.findByGioHang_IdAndSanPhamChiTiet_Id(
                gioHang.getId(), sanPhamChiTietId
        );

        int soLuongHienTai = (chiTiet != null ? chiTiet.getSoLuong() : 0);
        int tongSoLuong = soLuongHienTai + soLuong;

        int tonKho = spct.getSoLuong() == null ? 0 : spct.getSoLuong();
        if (tongSoLuong > tonKho) {
            throw new RuntimeException("Không đủ số lượng trong kho");
        }
        if (tonKho <= 0) {
            throw new RuntimeException("Sản phẩm đã hết hàng");
        }

        if (chiTiet != null) {
            chiTiet.setSoLuong(tongSoLuong);
        } else {
            chiTiet = GioHangChiTiet.builder()
                    .gioHang(gioHang)
                    .sanPhamChiTiet(spct)
                    .soLuong(soLuong)
                    .build();
        }

        chiTietRepo.save(chiTiet);
        System.out.println("[SAVE_CART_ITEM] cartId=" + gioHang.getId()
                + " itemId=" + (chiTiet.getId() == null ? "NEW" : chiTiet.getId())
                + " spctId=" + chiTiet.getSanPhamChiTiet().getId()
                + " qty=" + chiTiet.getSoLuong());
    }

    @Override
    public List<GioHangChiTiet> getCartByKhachHang(Integer khachHangId) {

        if (khachHangId == null) return List.of();

        var gioHangOpt = gioHangRepo.findByKhachHang_Id(khachHangId);
        if (gioHangOpt.isEmpty()) return List.of();

        Integer gioHangId = gioHangOpt.get().getId();

        List<GioHangChiTiet> items = chiTietRepo.findByGioHang_Id(gioHangId);

        List<GioHangChiTiet> validItems = new java.util.ArrayList<>();

        for (GioHangChiTiet item : items) {

            int tonKho = item.getSanPhamChiTiet().getSoLuong() == null
                    ? 0
                    : item.getSanPhamChiTiet().getSoLuong();

            // sản phẩm đã hết hàng → xóa khỏi giỏ
            if (tonKho <= 0) {
                chiTietRepo.delete(item);
                continue;
            }

            // nếu số lượng trong giỏ > tồn kho → giảm xuống
            if (item.getSoLuong() > tonKho) {
                item.setSoLuong(tonKho);
                chiTietRepo.save(item);
            }

            // ===== xử lý ảnh sản phẩm =====
            var spct = item.getSanPhamChiTiet();
            var sp = spct.getSanPham();
            var mauSac = spct.getMauSac();

            String hinhAnh = null;

            if (sp != null && sp.getHinhAnhList() != null && !sp.getHinhAnhList().isEmpty()) {

                // 1. ưu tiên ảnh theo màu
                if (mauSac != null) {
                    for (var img : sp.getHinhAnhList()) {
                        if (img.getMauSac() != null
                                && img.getMauSac().getId().equals(mauSac.getId())) {
                            hinhAnh = img.getDuongDanAnh();
                            break;
                        }
                    }
                }

                // 2. không có ảnh theo màu thì lấy ảnh chung
                if (hinhAnh == null) {
                    for (var img : sp.getHinhAnhList()) {
                        if (img.getMauSac() == null) {
                            hinhAnh = img.getDuongDanAnh();
                            break;
                        }
                    }
                }

                // 3. vẫn không có thì lấy ảnh đầu tiên
//                if (hinhAnh == null) {
//                    hinhAnh = sp.getHinhAnhList().get(0).getDuongDanAnh();
//                }
            }

            item.setHinhAnh(hinhAnh != null ? hinhAnh : "default.png");

            validItems.add(item);
        }

        return validItems;
    }

    @Override
    public void removeItem(Integer khachHangId, Integer cartItemId) {
        if (khachHangId == null) throw new RuntimeException("Thiếu khách hàng");
        if (cartItemId == null) return;

        GioHangChiTiet item = chiTietRepo.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        // ✅ ownership check
        Integer ownerId = item.getGioHang().getKhachHang().getId();
        if (!khachHangId.equals(ownerId)) {
            throw new RuntimeException("Không có quyền thao tác giỏ hàng");
        }

        chiTietRepo.delete(item);
    }

    @Override
    public void updateQuantity(Integer khachHangId, Integer cartItemId, Integer quantity) {
        if (khachHangId == null) throw new RuntimeException("Thiếu khách hàng");

        GioHangChiTiet item = chiTietRepo.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));

        Integer ownerId = item.getGioHang().getKhachHang().getId();
        if (!khachHangId.equals(ownerId)) {
            throw new RuntimeException("Không có quyền thao tác giỏ hàng");
        }

        if (quantity == null || quantity <= 0) {
            chiTietRepo.delete(item);
            return;
        }

        int tonKho = item.getSanPhamChiTiet().getSoLuong() == null
                ? 0
                : item.getSanPhamChiTiet().getSoLuong();

        if (quantity > tonKho) {
            throw new RuntimeException("Vượt quá số lượng tồn kho");
        }

        item.setSoLuong(quantity);
        chiTietRepo.save(item);
    }
}