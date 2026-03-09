package com.poly.shopquanao.service;

import com.poly.shopquanao.entity.GioHangChiTiet;

import java.util.List;

public interface CartService {

    void addToCart(Integer khachHangId,
                   Integer sanPhamChiTietId,
                   Integer soLuong);

    List<GioHangChiTiet> getCartByKhachHang(Integer khachHangId);

    void removeItem(Integer khachHangId, Integer cartItemId);

    void updateQuantity(Integer khachHangId, Integer cartItemId, Integer quantity);
}