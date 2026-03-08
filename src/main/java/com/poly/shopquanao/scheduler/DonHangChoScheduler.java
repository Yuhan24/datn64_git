package com.poly.shopquanao.scheduler;

import com.poly.shopquanao.entity.DonHang;
import com.poly.shopquanao.repository.admin.DonHangADRepository;
import com.poly.shopquanao.repository.client.DonHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DonHangChoScheduler {
    private final DonHangADRepository donHangADRepository;

    @Scheduled(fixedRate = 60000)
    public void xoaHoaDonChoQua30Phut() {
        LocalDateTime mocThoiGian = LocalDateTime.now().minusMinutes(30);

        List<DonHang> danhSachCanXoa =
                donHangADRepository.findByLoaiDonAndTrangThaiIdAndNgayTaoBefore(1, 1, mocThoiGian);

        for (DonHang donHang : danhSachCanXoa) {
            donHangADRepository.delete(donHang);
            System.out.println(">>> Đã xóa hóa đơn chờ: " + donHang.getMaDonHang());
        }
    }


}
