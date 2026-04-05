package com.poly.shopquanao.service.client;

import com.poly.shopquanao.dto.client.AttributeDTO;
import com.poly.shopquanao.dto.client.ProductDTO;
import com.poly.shopquanao.dto.client.ProductDetailDTO;
import com.poly.shopquanao.entity.SanPham;
import com.poly.shopquanao.entity.SanPhamChiTiet;
import com.poly.shopquanao.entity.SanPhamHinhAnh;
import com.poly.shopquanao.repository.client.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public List<ProductDTO> getProductsByCategoryName(String tenDanhMuc) {

        List<SanPham> sanPhams = sanPhamRepository.findAllByTenDanhMuc(tenDanhMuc);

        return sanPhams.stream()
                .map(this::mapToDTO) // dùng lại logic DTO hiện tại của bạn
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> filterProducts(List<Integer> mauIds,
                                           List<Integer> sizeIds,
                                           String price,
                                           String sort) {

        List<Integer> safeMauIds = (mauIds == null || mauIds.isEmpty()) ? List.of(-1) : mauIds;
        List<Integer> safeSizeIds = (sizeIds == null || sizeIds.isEmpty()) ? List.of(-1) : sizeIds;

        long mauCount = (mauIds == null) ? 0 : mauIds.size();
        long sizeCount = (sizeIds == null) ? 0 : sizeIds.size();

        List<SanPham> sanPhams = sanPhamRepository.filterProductsMixed(
                safeMauIds,
                mauCount,
                safeSizeIds,
                sizeCount,
                (price == null || price.isBlank()) ? null : price
        );

        List<ProductDTO> result = sanPhams.stream().map(this::mapToDTO).toList();

        if ("gia-tang".equals(sort)) {
            result = result.stream()
                    .sorted(Comparator.comparing(ProductDTO::getGiaMacDinh))
                    .toList();
        } else if ("gia-giam".equals(sort)) {
            result = result.stream()
                    .sorted(Comparator.comparing(ProductDTO::getGiaMacDinh).reversed())
                    .toList();
        }

        return result;
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Integer id) {
        SanPham sp = sanPhamRepository.findDetailById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm id=" + id));

        return mapToDTO(sp);
    }

    private ProductDTO mapToDTO(SanPham sp) {

        List<SanPhamChiTiet> activeChiTietList = sp.getChiTietList().stream()
                .filter(ct -> Boolean.TRUE.equals(ct.getTrangThai()))
                .sorted(Comparator
                        .comparing((SanPhamChiTiet ct) -> ct.getMauSac().getTenMau(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ct -> ct.getKichCo().getTenKichCo(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(SanPhamChiTiet::getId))
                .toList();

        List<ProductDetailDTO> details = activeChiTietList.stream()
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

        List<AttributeDTO> mauList = activeChiTietList.stream()
                .map(ct -> new AttributeDTO(
                        ct.getMauSac().getId(),
                        ct.getMauSac().getTenMau()
                ))
                .distinct()
                .toList();

        List<AttributeDTO> sizeList = activeChiTietList.stream()
                .map(ct -> new AttributeDTO(
                        ct.getKichCo().getId(),
                        ct.getKichCo().getTenKichCo()
                ))
                .distinct()
                .toList();

        BigDecimal giaMacDinh = activeChiTietList.stream()
                .findFirst()
                .map(SanPhamChiTiet::getGiaBan)
                .orElse(BigDecimal.ZERO);

        List<SanPhamHinhAnh> sortedImages = sp.getHinhAnhList().stream()
                .filter(ha -> ha.getDuongDanAnh() != null && !ha.getDuongDanAnh().isBlank())
                .sorted(Comparator.comparing(SanPhamHinhAnh::getId))
                .toList();

        List<String> images = sortedImages.stream()
                .map(SanPhamHinhAnh::getDuongDanAnh)
                .distinct()
                .toList();

        var imageByColor = sortedImages.stream()
                .filter(ha -> ha.getMauSac() != null)
                .collect(Collectors.groupingBy(
                        ha -> ha.getMauSac().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(SanPhamHinhAnh::getDuongDanAnh, Collectors.toList())
                ));

        Integer firstColorId = activeChiTietList.stream()
                .findFirst()
                .map(ct -> ct.getMauSac().getId())
                .orElse(null);

        String hinhAnh = null;

        if (firstColorId != null) {
            List<String> colorImages = imageByColor.get(firstColorId);
            if (colorImages != null && !colorImages.isEmpty()) {
                hinhAnh = colorImages.get(0);
            }
        }

        if (hinhAnh == null) {
            hinhAnh = images.stream().filter(Objects::nonNull).findFirst().orElse(null);
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