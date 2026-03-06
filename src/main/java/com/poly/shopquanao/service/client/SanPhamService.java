package com.poly.shopquanao.service.client;

import com.poly.shopquanao.dto.client.AttributeDTO;
import com.poly.shopquanao.dto.client.ProductDTO;
import com.poly.shopquanao.dto.client.ProductDetailDTO;
import com.poly.shopquanao.entity.SanPham;
import com.poly.shopquanao.entity.SanPhamHinhAnh;
import com.poly.shopquanao.repository.client.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        List<SanPham> sanPhams = sanPhamRepository.findAllWithDetails();
        return sanPhams.stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> filterProducts(List<Integer> mauIds,
                                           List<Integer> sizeIds,
                                           String price,
                                           String sort) {

        List<Integer> safeMauIds = (mauIds == null || mauIds.isEmpty()) ? null : mauIds;
        List<Integer> safeSizeIds = (sizeIds == null) ? List.of() : sizeIds;

        List<SanPham> sanPhams = sanPhamRepository.filterProductsMixed(
                safeMauIds,
                safeSizeIds.isEmpty() ? List.of(-1) : safeSizeIds,
                safeSizeIds.size(),
                (price == null || price.isBlank()) ? null : price
        );

        List<ProductDTO> result = sanPhams.stream().map(this::mapToDTO).toList();

        if ("gia-tang".equals(sort)) {
            result = result.stream()
                    .sorted(java.util.Comparator.comparing(ProductDTO::getGiaMacDinh))
                    .toList();
        } else if ("gia-giam".equals(sort)) {
            result = result.stream()
                    .sorted(java.util.Comparator.comparing(ProductDTO::getGiaMacDinh).reversed())
                    .toList();
        }

        return result;
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Integer id) {
        SanPham sp = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm id=" + id));

        return mapToDTO(sp);
    }

    private ProductDTO mapToDTO(SanPham sp) {

        var chiTietList = sp.getChiTietList();

        List<ProductDetailDTO> details = chiTietList.stream()
                .map(ct -> new ProductDetailDTO(
                        ct.getId(),
                        ct.getMauSac().getId(),
                        ct.getMauSac().getTenMau(),
                        ct.getKichCo().getId(),
                        ct.getKichCo().getTenKichCo(),
                        ct.getGiaBan(),
                        ct.getSoLuong()
                ))
                .toList();

        var mauList = chiTietList.stream()
                .map(ct -> new AttributeDTO(
                        ct.getMauSac().getId(),
                        ct.getMauSac().getTenMau()
                ))
                .distinct()
                .toList();

        var sizeList = chiTietList.stream()
                .map(ct -> new AttributeDTO(
                        ct.getKichCo().getId(),
                        ct.getKichCo().getTenKichCo()
                ))
                .distinct()
                .toList();

        BigDecimal giaMacDinh = chiTietList.stream()
                .findFirst()
                .map(ct -> ct.getGiaBan())
                .orElse(BigDecimal.ZERO);


        // map ảnh theo màu
        var imageByColor = sp.getHinhAnhList().stream()
                .filter(ha -> ha.getMauSac() != null && ha.getDuongDanAnh() != null)
                .collect(java.util.stream.Collectors.toMap(
                        ha -> ha.getMauSac().getId(),
                        SanPhamHinhAnh::getDuongDanAnh,
                        (a, b) -> a
                ));

        // ⭐ gallery ảnh
        List<String> images = sp.getHinhAnhList().stream()
                .map(SanPhamHinhAnh::getDuongDanAnh)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();


        Integer firstColorId = chiTietList.stream()
                .findFirst()
                .map(ct -> ct.getMauSac().getId())
                .orElse(null);

        String hinhAnh = null;

        if (firstColorId != null) {
            hinhAnh = imageByColor.get(firstColorId);
        }

        if (hinhAnh == null) {
            hinhAnh = images.stream().findFirst().orElse(null);
        }

        return new ProductDTO(
                sp.getId(),
                sp.getTenSanPham(),
                hinhAnh,
                giaMacDinh,
                details,
                mauList,
                sizeList,
                imageByColor,
                images
        );
    }

}