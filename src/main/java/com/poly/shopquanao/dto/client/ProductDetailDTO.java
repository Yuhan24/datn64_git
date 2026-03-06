package com.poly.shopquanao.dto.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProductDetailDTO {
    private Integer id;
    private Integer mauSacId;
    private String tenMau;
    private Integer kichCoId;
    private String tenKichCo;
    private BigDecimal giaBan;
    private Integer soLuong;
}