package com.poly.shopquanao.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity trạng thái đơn hàng – map bảng [trang_thai_don_hang].
 *
 * Các giá trị mặc định trong DB:
 *   1 = Chờ xác nhận
 *   2 = Đang giao
 *   3 = Hoàn thành
 *   4 = Đã hủy
 */
@Entity
@Table(name = "trang_thai_don_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrangThaiDonHang {

    @Id
    private Integer id;                 // Không IDENTITY, giá trị cố định

    @Column(name = "ten_trang_thai")
    private String tenTrangThai;        // VD: "Chờ xác nhận", "Đang giao"...
}
