package com.poly.shopquanao.controller.client;

import com.poly.shopquanao.dto.client.ProductDetailDTO;
import com.poly.shopquanao.repository.client.SanPhamChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SanPhamApiController {

    private final SanPhamChiTietRepository repo;

    @GetMapping("/chitiet/{id}")
    public ProductDetailDTO getChiTiet(@PathVariable Integer id) {
        var ct = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));

        return new ProductDetailDTO(
                ct.getId(),
                ct.getMauSac().getId(),
                ct.getMauSac().getTenMau(),
                ct.getKichCo().getId(),
                ct.getKichCo().getTenKichCo(),
                ct.getGiaBan(),
                ct.getSoLuong()
        );
    }
}