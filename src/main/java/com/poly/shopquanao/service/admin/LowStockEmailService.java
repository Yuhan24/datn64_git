package com.poly.shopquanao.service.admin;


import com.poly.shopquanao.dto.admin.LowStockItemDTO;
import com.poly.shopquanao.entity.SanPhamChiTiet;
import com.poly.shopquanao.repository.admin.SanPhamChiTietADMRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Service gửi email báo cáo sản phẩm hết hàng lúc 20:00 hàng ngày.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LowStockEmailService {


    private final SanPhamChiTietADMRepository sanPhamChiTietRepo;
    private final JavaMailSender mailSender;


    @Value("${app.lowstock.threshold:10}")
    private int threshold;


    @Value("${app.lowstock.email-to:admin@shopquanao.com}")
    private String emailTo;


    /**
     * Chạy lúc 20:00 hàng ngày (giờ server).
     * Cron: giây phút giờ ngày tháng thứ
     */
//    @Scheduled(cron = "0 0 20 * * *")//
    @Scheduled(cron = "0 */1 * * * *")  // Chạy mỗi 1 phút để test
    public void sendLowStockReport() {
        log.info("[LOW-STOCK] Bắt đầu kiểm tra sản phẩm hết hàng (ngưỡng < {})", threshold);


        List<SanPhamChiTiet> lowStockItems = sanPhamChiTietRepo.findLowStockItems(threshold);


        if (lowStockItems.isEmpty()) {
            log.info("[LOW-STOCK] Không có sản phẩm nào hết hàng. Bỏ qua gửi email.");
            return;
        }


        List<LowStockItemDTO> dtoList = mapToDTO(lowStockItems);


        try {
            String htmlContent = buildEmailHtml(dtoList);
            sendHtmlEmail(htmlContent, dtoList.size());
            log.info("[LOW-STOCK] Đã gửi email báo cáo {} sản phẩm hết hàng đến {}", dtoList.size(), emailTo);
        } catch (MessagingException e) {
            log.error("[LOW-STOCK] Lỗi gửi email: {}", e.getMessage(), e);
        }
    }


    /**
     * Map entity sang DTO (dùng chung cho Dashboard và Email).
     */
    public List<LowStockItemDTO> mapToDTO(List<SanPhamChiTiet> items) {
        return items.stream().map(spct -> LowStockItemDTO.builder()
                .id(spct.getId())
                .maSanPham(spct.getSanPham().getMaSanPham())
                .tenSanPham(spct.getSanPham().getTenSanPham())
                .tenMauSac(spct.getMauSac().getTenMau())
                .tenKichCo(spct.getKichCo().getTenKichCo())
                .soLuong(spct.getSoLuong())
                .giaBan(spct.getGiaBan())
                .build()
        ).collect(Collectors.toList());
    }


    private void sendHtmlEmail(String htmlContent, int totalItems) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");


        helper.setTo(emailTo);
        helper.setSubject("⚠️ Báo cáo sản phẩm hết hàng - " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " (" + totalItems + " sản phẩm)");
        helper.setText(htmlContent, true);


        mailSender.send(message);
    }


    private String buildEmailHtml(List<LowStockItemDTO> items) {
        DecimalFormat df = new DecimalFormat("#,###");
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));


        long hetHang = items.stream().filter(i -> i.getSoLuong() == null || i.getSoLuong() <= 0).count();
        long sapHet = items.size() - hetHang;


        StringBuilder sb = new StringBuilder();


        sb.append("""
           <div style="font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; background: #f9fafb; padding: 20px;">
               <div style="background: #ffffff; border-radius: 12px; padding: 24px; border: 1px solid #e5e7eb;">
                   <h2 style="color: #111827; margin-top: 0;">⚠️ Báo cáo sản phẩm hết hàng</h2>
                   <p style="color: #6b7280; font-size: 14px;">Thời gian: %s</p>


                   <div style="display: flex; gap: 16px; margin-bottom: 20px;">
                       <div style="flex: 1; background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 16px; text-align: center;">
                           <div style="font-size: 28px; font-weight: 900; color: #dc2626;">%d</div>
                           <div style="color: #991b1b; font-size: 13px;">Hết hàng (= 0)</div>
                       </div>
                       <div style="flex: 1; background: #fffbeb; border: 1px solid #fde68a; border-radius: 8px; padding: 16px; text-align: center;">
                           <div style="font-size: 28px; font-weight: 900; color: #d97706;">%d</div>
                           <div style="color: #92400e; font-size: 13px;">Sắp hết (1-9)</div>
                       </div>
                       <div style="flex: 1; background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; padding: 16px; text-align: center;">
                           <div style="font-size: 28px; font-weight: 900; color: #2563eb;">%d</div>
                           <div style="color: #1e40af; font-size: 13px;">Tổng cảnh báo</div>
                       </div>
                   </div>
           """.formatted(now, hetHang, sapHet, items.size()));


        sb.append("""
                   <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
                       <thead>
                           <tr style="background: #f3f4f6;">
                               <th style="padding: 10px 8px; text-align: left; border-bottom: 2px solid #e5e7eb;">STT</th>
                               <th style="padding: 10px 8px; text-align: left; border-bottom: 2px solid #e5e7eb;">Mã SP</th>
                               <th style="padding: 10px 8px; text-align: left; border-bottom: 2px solid #e5e7eb;">Tên sản phẩm</th>
                               <th style="padding: 10px 8px; text-align: left; border-bottom: 2px solid #e5e7eb;">Màu sắc</th>
                               <th style="padding: 10px 8px; text-align: left; border-bottom: 2px solid #e5e7eb;">Kích cỡ</th>
                               <th style="padding: 10px 8px; text-align: right; border-bottom: 2px solid #e5e7eb;">Tồn kho</th>
                               <th style="padding: 10px 8px; text-align: right; border-bottom: 2px solid #e5e7eb;">Giá bán</th>
                               <th style="padding: 10px 8px; text-align: center; border-bottom: 2px solid #e5e7eb;">Trạng thái</th>
                           </tr>
                       </thead>
                       <tbody>
           """);


        int stt = 1;
        for (LowStockItemDTO item : items) {
            String bgColor = stt % 2 == 0 ? "#f9fafb" : "#ffffff";
            String stockColor;
            String statusBg;
            String statusText;


            if (item.getSoLuong() == null || item.getSoLuong() <= 0) {
                stockColor = "#dc2626";
                statusBg = "#fef2f2";
                statusText = "🔴 Hết hàng";
            } else {
                stockColor = "#d97706";
                statusBg = "#fffbeb";
                statusText = "🟡 Sắp hết";
            }


            String giaBanStr = item.getGiaBan() != null ? df.format(item.getGiaBan()) + " ₫" : "-";
            int soLuong = item.getSoLuong() != null ? item.getSoLuong() : 0;


            sb.append(String.format("""
                           <tr style="background: %s;">
                               <td style="padding: 10px 8px; border-bottom: 1px solid #e5e7eb;">%d</td>
                               <td style="padding: 10px 8px; border-bottom: 1px solid #e5e7eb;">%s</td>
                               <td style="padding: 10px 8px; border-bottom: 1px solid #e5e7eb; font-weight: 600;">%s</td>
                               <td style="padding: 10px 8px; border-bottom: 1px solid #e5e7eb;">%s</td>
                               <td style="padding: 10px 8px; border-bottom: 1px solid #e5e7eb;">%s</td>
                               <td style="padding: 10px 8px; border-bottom: 1px solid #e5e7eb; text-align: right; font-weight: 700; color: %s;">%d</td>
                               <td style="padding: 10px 8px; border-bottom: 1px solid #e5e7eb; text-align: right;">%s</td>
                               <td style="padding: 10px 8px; border-bottom: 1px solid #e5e7eb; text-align: center;">
                                   <span style="background: %s; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600;">%s</span>
                               </td>
                           </tr>
               """, bgColor, stt, item.getMaSanPham(), item.getTenSanPham(),
                    item.getTenMauSac(), item.getTenKichCo(),
                    stockColor, soLuong, giaBanStr,
                    statusBg, statusText));


            stt++;
        }


        sb.append("""
                       </tbody>
                   </table>


                   <p style="color: #9ca3af; font-size: 12px; margin-top: 20px; text-align: center;">
                       Email tự động từ hệ thống Shop Quần Áo – Vui lòng không trả lời email này.
                   </p>
               </div>
           </div>
       """);


        return sb.toString();
    }
}



