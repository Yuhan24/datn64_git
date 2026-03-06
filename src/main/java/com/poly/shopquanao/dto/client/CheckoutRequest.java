package com.poly.shopquanao.dto.client;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckoutRequest {
    private List<Integer> selectedIds;

    private String tenNguoiNhan;
    private String soDienThoai;
    private String diaChiGiaoHang;
    private String ghiChu;

    private String phuongThucThanhToan;
    private String maKhuyenMai;
}