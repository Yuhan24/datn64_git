package com.poly.shopquanao.service.admin;


import com.poly.shopquanao.entity.DonHang;
import com.poly.shopquanao.entity.DonHangChiTiet;
import com.poly.shopquanao.repository.admin.DonHangADRepository;
import com.poly.shopquanao.repository.admin.DonHangChiTietADRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class PosOrderCleanupService {

    private final DonHangADRepository donHangADRepository;
    private final DonHangChiTietADRepository
            donHangChiTietADRepository;

    @Scheduled(fixedRate = 60000) // mỗi 60 giây chạy 1 lần
    @Transactional
    public void autoDeleteExpiredPosOrders() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(30);

        List<DonHang> expiredOrders =
                donHangADRepository.findByLoaiDonAndTrangThaiThanhToanAndNgayTaoBefore(
                        1,
                        "CHUA_THANH_TOAN",
                        expiredTime
                );

        for (DonHang donHang : expiredOrders) {
            List<DonHangChiTiet> chiTietList =
                    donHangChiTietADRepository.findByDonHang_Id(donHang.getId());

            if (!chiTietList.isEmpty()) {
                donHangChiTietADRepository.deleteAll(chiTietList);
            }

            donHangADRepository.delete(donHang);
        }
    }
}
