package com.poly.shopquanao.controller.client;

import com.poly.shopquanao.service.client.GoongShippingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipping")
public class ShippingController {

    private final GoongShippingService goongShippingService;

    @PostMapping("/fee")
    public ResponseEntity<?> calculateFee(@RequestBody ShippingFeeRequest request) {
        if (isBlank(request.getTinhThanh()) || isBlank(request.getPhuongXa())) {
            return ResponseEntity.badRequest()
                    .body(error("Vui lòng chọn đầy đủ tỉnh/thành và phường/xã"));
        }

        String fullAddress = buildAddress(
                request.getDiaChiChiTiet(),
                request.getPhuongXa(),
                request.getTinhThanh()
        );

        try {
            GoongShippingService.ShippingResult result =
                    goongShippingService.calculateByProvince(request.getTinhThanh());

            ShippingFeeResponse response = new ShippingFeeResponse();
            response.setSuccess(true);
            response.setMessage("OK");
            response.setPhiVanChuyen(result.getPhiVanChuyen());
            response.setResolvedAddress(fullAddress);
            response.setDistanceKm(result.getDistanceKm());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(error("Không tính được phí ship cho khu vực: "
                            + buildAddress(request.getPhuongXa(), request.getTinhThanh())
                            + ". " + e.getMessage()));
        }
    }

    private String buildAddress(String... parts) {
        return Stream.of(parts)
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", false);
        map.put("message", message);
        map.put("phiVanChuyen", BigDecimal.ZERO);
        map.put("resolvedAddress", "");
        map.put("distanceKm", 0);
        return map;
    }

    @Data
    public static class ShippingFeeRequest {
        private String tinhThanh;
        private String phuongXa;
        private String diaChiChiTiet;
        private String provinceCode;
        private String wardCode;
    }

    @Data
    public static class ShippingFeeResponse {
        private boolean success;
        private String message;
        private BigDecimal phiVanChuyen;
        private String resolvedAddress;
        private double distanceKm;
    }
}