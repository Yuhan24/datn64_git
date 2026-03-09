package com.poly.shopquanao.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kich_co")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KichCo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_kich_co", unique = true, nullable = false)
    private String tenKichCo;

    @Column(name = "trang_thai")
    private Boolean trangThai;
}