package com.poly.shopquanao.entity;
import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "thuong_hieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThuongHieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_thuong_hieu", unique = true, nullable = false)
    private String tenThuongHieu;

    @Column(name = "trang_thai")
    private Boolean trangThai;
}