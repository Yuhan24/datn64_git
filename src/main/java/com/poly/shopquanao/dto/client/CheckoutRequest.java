package com.poly.shopquanao.dto.client;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutRequest {
    private List<Integer> selectedIds;

    private String tenNguoiNhan;
    private String soDienThoai;
    private String diaChiGiaoHang;
    private String ghiChu;
    private String phuongThucThanhToan;
    private String maKhuyenMai;

    private String tinhThanh;
    private String phuongXa;

    private String provinceCode;
    private String wardCode;

    private BigDecimal phiVanChuyen;
}