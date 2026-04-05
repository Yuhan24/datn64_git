package com.poly.shopquanao.service.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GoongShippingService {

    @Value("${goong.api.key}")
    private String apiKey;

    @Value("${shop.address}")
    private String shopAddress;

    private final RestTemplate restTemplate;

    /**
     * Tính ship theo tỉnh/thành:
     * - vẫn dùng Goong để geocode
     * - không dùng DistanceMatrix
     * - tính khoảng cách chim bay giữa shop và tỉnh/thành
     */
    public ShippingResult calculateByProvince(String province) {
        String normalizedProvince = normalize(province);

        if (isBlank(normalizedProvince)) {
            throw new RuntimeException("Thiếu tỉnh/thành giao hàng");
        }

        LatLng shopLatLng = geocodeShopAddress();
        LatLng provinceLatLng = geocodeProvince(normalizedProvince);

        double distanceKm = calculateStraightLineDistanceKm(shopLatLng, provinceLatLng);
        BigDecimal shippingFee = calculateFeeByProvinceDistance(distanceKm);

        System.out.println("===== DEBUG SHIPPING =====");
        System.out.println("Province: " + normalizedProvince);
        System.out.println("Shop address: " + normalize(shopAddress));
        System.out.println("Shop lat/lng: " + shopLatLng.getLat() + ", " + shopLatLng.getLng());
        System.out.println("Province lat/lng: " + provinceLatLng.getLat() + ", " + provinceLatLng.getLng());
        System.out.println("Distance KM: " + distanceKm);
        System.out.println("Fee: " + shippingFee);
        System.out.println("=========================");

        ShippingResult result = new ShippingResult();
        result.setPhiVanChuyen(shippingFee);
        result.setResolvedAddress(normalizedProvince);
        result.setDistanceKm(distanceKm);
        return result;
    }

    private LatLng geocodeShopAddress() {
        String normalizedShopAddress = normalize(shopAddress);

        if (isBlank(normalizedShopAddress)) {
            throw new RuntimeException("Cấu hình địa chỉ cửa hàng không hợp lệ");
        }

        return geocodeWithFallback(buildShopCandidates(normalizedShopAddress));
    }

    /**
     * Geocode theo tỉnh/thành.
     * Ưu tiên query rõ ràng hơn để giảm sai lệch.
     */
    private LatLng geocodeProvince(String province) {
        List<String> candidates = new ArrayList<>();
        candidates.add(province + ", Việt Nam");
        candidates.add("Tỉnh " + province + ", Việt Nam");
        candidates.add("Thành phố " + province + ", Việt Nam");
        candidates.add(province);
        return geocodeWithFallback(candidates);
    }

    private LatLng geocodeWithFallback(List<String> candidates) {
        RuntimeException lastError = null;

        for (String candidate : candidates) {
            try {
                return geocode(candidate);
            } catch (RuntimeException ex) {
                lastError = ex;
            }
        }

        if (lastError != null) {
            throw lastError;
        }

        throw new RuntimeException("Không lấy được tọa độ từ Goong");
    }

    private List<String> buildShopCandidates(String shopAddress) {
        Set<String> candidates = new LinkedHashSet<>();
        String normalized = normalize(shopAddress);

        if (!isBlank(normalized)) {
            candidates.add(normalized);
            candidates.add(normalized + ", Việt Nam");
        }

        return new ArrayList<>(candidates);
    }

    private LatLng geocode(String address) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://rsapi.goong.io/Geocode")
                    .queryParam("address", address)
                    .queryParam("api_key", apiKey)
                    .toUriString();

            JsonNode root = restTemplate.getForObject(url, JsonNode.class);

            if (root == null) {
                throw new RuntimeException("Goong Geocode không trả dữ liệu");
            }

            String status = root.path("status").asText("");
            JsonNode results = root.path("results");

            if (!"OK".equalsIgnoreCase(status) || !results.isArray() || results.isEmpty()) {
                throw new RuntimeException("Không lấy được tọa độ cho địa chỉ: " + address);
            }

            JsonNode first = results.get(0);
            JsonNode location = first.path("geometry").path("location");

            if (location.isMissingNode()
                    || location.path("lat").isMissingNode()
                    || location.path("lng").isMissingNode()) {
                throw new RuntimeException("Goong không trả về lat/lng hợp lệ cho địa chỉ: " + address);
            }

            double lat = location.path("lat").asDouble(Double.NaN);
            double lng = location.path("lng").asDouble(Double.NaN);

            if (Double.isNaN(lat) || Double.isNaN(lng)) {
                throw new RuntimeException("Tọa độ trả về không hợp lệ cho địa chỉ: " + address);
            }

            LatLng latLng = new LatLng();
            latLng.setLat(lat);
            latLng.setLng(lng);
            return latLng;
        } catch (RestClientException ex) {
            throw new RuntimeException("Lỗi gọi Goong Geocode API", ex);
        }
    }

    /**
     * Tính khoảng cách chim bay giữa 2 tọa độ bằng Haversine.
     */
    private double calculateStraightLineDistanceKm(LatLng origin, LatLng destination) {
        final double earthRadiusKm = 6371.0;

        double lat1 = Math.toRadians(origin.getLat());
        double lon1 = Math.toRadians(origin.getLng());
        double lat2 = Math.toRadians(destination.getLat());
        double lon2 = Math.toRadians(destination.getLng());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double km = earthRadiusKm * c;

        return Math.round(km * 100.0) / 100.0;
    }

    /**
     * Công thức ship theo khoảng cách chung giữa các tỉnh.
     */
    private BigDecimal calculateFeeByProvinceDistance(double km) {
        if (km <= 0) {
            return BigDecimal.valueOf(15000);
        }
        if (km <= 30) {
            return BigDecimal.valueOf(15000);
        }
        if (km <= 100) {
            return BigDecimal.valueOf(18000);
        }
        if (km <= 200) {
            return BigDecimal.valueOf(21000);
        }
        if (km <= 400) {
            return BigDecimal.valueOf(24000);
        }
        if (km <= 800) {
            return BigDecimal.valueOf(27000);
        }

        double extraKm = km - 800;
        long blocks = (long) Math.ceil(extraKm / 100.0);
        return BigDecimal.valueOf(27000 + (blocks * 2000L));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*,\\s*", ", ")
                .replaceAll("^,\\s*", "")
                .replaceAll("\\s*,\\s*$", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class ShippingResult {
        private BigDecimal phiVanChuyen;
        private String resolvedAddress;
        private double distanceKm;

        public BigDecimal getPhiVanChuyen() {
            return phiVanChuyen;
        }

        public void setPhiVanChuyen(BigDecimal phiVanChuyen) {
            this.phiVanChuyen = phiVanChuyen;
        }

        public String getResolvedAddress() {
            return resolvedAddress;
        }

        public void setResolvedAddress(String resolvedAddress) {
            this.resolvedAddress = resolvedAddress;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        public void setDistanceKm(double distanceKm) {
            this.distanceKm = distanceKm;
        }
    }

    public static class LatLng {
        private double lat;
        private double lng;

        public LatLng() {
        }

        public LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }
}