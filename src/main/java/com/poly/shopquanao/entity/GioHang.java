package com.poly.shopquanao.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gio_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GioHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // giữ nguyên entity KhachHang theo yêu cầu bạn
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id", nullable = false, unique = true)
    private KhachHang khachHang;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "gioHang", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties("gioHang")
    private List<GioHangChiTiet> chiTietList = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
    }
}