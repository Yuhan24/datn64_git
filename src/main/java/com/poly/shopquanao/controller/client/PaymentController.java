package com.poly.shopquanao.controller.client;

import com.poly.shopquanao.config.VnPayConfig;
import com.poly.shopquanao.entity.DonHang;
import com.poly.shopquanao.entity.DonHangChiTiet;
import com.poly.shopquanao.entity.GioHang;
import com.poly.shopquanao.entity.GioHangChiTiet;
import com.poly.shopquanao.entity.SanPhamChiTiet;
import com.poly.shopquanao.repository.client.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final KhuyenMaiClientRepository khuyenMaiClientRepository;
    private final DonHangRepository donHangRepository;
    private final GioHangRepository gioHangRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    @GetMapping("/create")
    public String createPayment(HttpServletRequest request,
                                @RequestParam("amount") BigDecimal amount,
                                @RequestParam("orderCode") String orderCode) throws Exception {

        DonHang donHang = donHangRepository.findByMaDonHang(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if ("DA_THANH_TOAN".equals(donHang.getTrangThaiThanhToan())) {
            return "redirect:/order/success/" + donHang.getId();
        }

        if (!"VNPAY".equals(donHang.getPhuongThucThanhToan())) {
            return "redirect:/order/detail/" + donHang.getId();
        }

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", VnPayConfig.vnp_TmnCode);
        params.put("vnp_Amount", amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");

        String txnRef = orderCode + "_" + System.currentTimeMillis();
        params.put("vnp_TxnRef", txnRef);

        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderCode);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl);
        params.put("vnp_IpAddr", getClientIp(request));

        // Tạm không ép VNPAYQR để tránh code 76
        // params.put("vnp_BankCode", "VNPAYQR");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        ZonedDateTime expire = now.plusMinutes(15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        params.put("vnp_CreateDate", now.format(formatter));
        params.put("vnp_ExpireDate", expire.format(formatter));

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String field : fieldNames) {
            String value = params.get(field);
            if (value == null || value.isEmpty()) continue;

            String encoded = URLEncoder.encode(value, StandardCharsets.US_ASCII);

            if (hashData.length() > 0) {
                hashData.append('&');
                query.append('&');
            }

            hashData.append(field).append('=').append(encoded);
            query.append(field).append('=').append(encoded);
        }

        String secureHash = VnPayConfig.hmacSHA512(
                VnPayConfig.vnp_HashSecret,
                hashData.toString()
        );

        query.append("&vnp_SecureHash=").append(secureHash);

        return "redirect:" + VnPayConfig.vnp_PayUrl + "?" + query;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @GetMapping("/vnpay-return")
    @Transactional
    public String paymentReturn(HttpServletRequest request) throws Exception {

        Map<String, String> fields = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {
            String fieldName = paramNames.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnpSecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue == null || fieldValue.isEmpty()) {
                continue;
            }

            if (hashData.length() > 0) {
                hashData.append('&');
            }

            hashData.append(fieldName)
                    .append('=')
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
        }

        String calculatedHash = VnPayConfig.hmacSHA512(
                VnPayConfig.vnp_HashSecret,
                hashData.toString()
        );

        String txnRef = request.getParameter("vnp_TxnRef");
        String orderCode = txnRef;
        if (txnRef != null && txnRef.contains("_")) {
            orderCode = txnRef.substring(0, txnRef.indexOf("_"));
        }

        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionStatus = request.getParameter("vnp_TransactionStatus");

        DonHang donHang = donHangRepository.findByMaDonHang(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Nếu đơn đã thanh toán rồi thì không xử lý lại nữa
        if ("DA_THANH_TOAN".equals(donHang.getTrangThaiThanhToan())) {
            return "redirect:/order/success/" + donHang.getId();
        }

        // Hash không hợp lệ: không trừ kho, không đóng đơn, giữ chờ thanh toán để có thể thử lại
        if (vnpSecureHash == null || !calculatedHash.equals(vnpSecureHash)) {
            donHang.setTrangThaiThanhToan("CHO_THANH_TOAN");
            donHangRepository.save(donHang);
            return "redirect:/order/detail/" + donHang.getId() + "?paymentError=invalid_hash";
        }

        // Chỉ khi cả 2 cùng là 00 mới coi là thanh toán thành công thật
        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {

            // 1. kiểm tra tồn kho trước
            for (DonHangChiTiet ct : donHang.getChiTietList()) {
                SanPhamChiTiet spct = sanPhamChiTietRepository.findByIdForUpdate(ct.getSanPhamChiTiet().getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

                if (spct.getSoLuong() == null || spct.getSoLuong() < ct.getSoLuong()) {
                    donHang.setTrangThaiThanhToan("THAT_BAI");
                    donHangRepository.save(donHang);
                    return "redirect:/order/detail/" + donHang.getId() + "?paymentError=outOfStock";
                }
            }

            // 2. trừ kho
            for (DonHangChiTiet ct : donHang.getChiTietList()) {
                SanPhamChiTiet spct = sanPhamChiTietRepository.findByIdForUpdate(ct.getSanPhamChiTiet().getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

                spct.setSoLuong(spct.getSoLuong() - ct.getSoLuong());
                sanPhamChiTietRepository.save(spct);
            }

            // 3. xóa khỏi giỏ hàng
            GioHang gioHang = gioHangRepository.findByKhachHang_Id(donHang.getKhachHang().getId())
                    .orElse(null);

            if (gioHang != null) {
                for (DonHangChiTiet ct : donHang.getChiTietList()) {
                    GioHangChiTiet ghct = gioHangChiTietRepository.findByGioHang_IdAndSanPhamChiTiet_Id(
                            gioHang.getId(),
                            ct.getSanPhamChiTiet().getId()
                    );

                    if (ghct != null) {
                        gioHangChiTietRepository.delete(ghct);
                    }
                }
                gioHangChiTietRepository.flush();
            }

            // 4. cập nhật trạng thái
            donHang.setTrangThaiThanhToan("DA_THANH_TOAN");
            donHangRepository.save(donHang);

            return "redirect:/order/success/" + donHang.getId();
        }

        // Khách hủy / thanh toán lỗi: không trừ kho, vẫn giữ đơn để có thể thanh toán lại
        donHang.setTrangThaiThanhToan("CHO_THANH_TOAN");
        donHangRepository.save(donHang);

        return "redirect:/order/detail/" + donHang.getId() + "?paymentError=failed";
    }
}