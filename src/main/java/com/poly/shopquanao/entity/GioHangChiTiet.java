package com.poly.shopquanao.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "gio_hang_chi_tiet",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"gio_hang_id", "san_pham_chi_tiet_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GioHangChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "gio_hang_id", nullable = false)
    private GioHang gioHang;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_chi_tiet_id", nullable = false)
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;
}