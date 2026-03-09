package com.poly.shopquanao.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "danh_muc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_danh_muc", unique = true, nullable = false)
    private String tenDanhMuc;

    @Column(name = "trang_thai")
    private Boolean trangThai;
}