package com.poly.shopquanao.dto.admin;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;


/**
 * DTO hiển thị sản phẩm sắp hết hàng trên Dashboard & trong email báo cáo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockItemDTO {


    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String tenMauSac;
    private String tenKichCo;
    private Integer soLuong;       // null hoặc < threshold
    private BigDecimal giaBan;


    /**
     * Mức cảnh báo:
     *   - "het_hang"  : soLuong == null hoặc 0
     *   - "sap_het"   : 1 <= soLuong < threshold
     */
    public String getMucCanhBao() {
        if (soLuong == null || soLuong <= 0) {
            return "het_hang";
        }
        return "sap_het";
    }


    public String getMucCanhBaoLabel() {
        if (soLuong == null || soLuong <= 0) {
            return "Hết hàng";
        }
        return "Sắp hết";
    }
}



