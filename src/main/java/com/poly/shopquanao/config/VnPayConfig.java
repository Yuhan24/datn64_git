package com.poly.shopquanao.config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class VnPayConfig {

    // THAY BẰNG THÔNG TIN SANDBOX THẬT
    public static String vnp_TmnCode = "LIQACMVY";
    public static String vnp_HashSecret = "ZMOBKWQML3NUHG7XB4PWVREVY5CH50JU";

    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "http://localhost:8080/payment/vnpay-return";

    public static String hmacSHA512(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                "HmacSHA512"
        );
        hmac.init(secretKey);

        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hash.append('0');
            }
            hash.append(hex);
        }
        return hash.toString();
    }
}