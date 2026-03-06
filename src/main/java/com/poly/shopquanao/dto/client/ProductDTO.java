package com.poly.shopquanao.dto.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ProductDTO {

    private Integer id;
    private String tenSanPham;
    private String hinhAnh;
    private BigDecimal giaMacDinh;

    private List<ProductDetailDTO> chiTietList;
    private List<AttributeDTO> mauList;
    private List<AttributeDTO> sizeList;

    private Map<Integer, String> imageByColor;

    // ⭐ thêm gallery
    private List<String> images;

    public ProductDTO(Integer id,
                      String tenSanPham,
                      String hinhAnh,
                      BigDecimal giaMacDinh,
                      List<ProductDetailDTO> chiTietList,
                      List<AttributeDTO> mauList,
                      List<AttributeDTO> sizeList,
                      Map<Integer, String> imageByColor,
                      List<String> images) {

        this.id = id;
        this.tenSanPham = tenSanPham;
        this.hinhAnh = hinhAnh;
        this.giaMacDinh = giaMacDinh;
        this.chiTietList = chiTietList;
        this.mauList = mauList;
        this.sizeList = sizeList;
        this.imageByColor = imageByColor;
        this.images = images;
    }

    public Integer getId() { return id; }
    public String getTenSanPham() { return tenSanPham; }
    public String getHinhAnh() { return hinhAnh; }
    public BigDecimal getGiaMacDinh() { return giaMacDinh; }

    public List<ProductDetailDTO> getChiTietList() { return chiTietList; }
    public List<AttributeDTO> getMauList() { return mauList; }
    public List<AttributeDTO> getSizeList() { return sizeList; }

    public Map<Integer, String> getImageByColor() { return imageByColor; }

    public List<String> getImages() { return images; }
}