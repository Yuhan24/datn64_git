package com.poly.shopquanao.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vai_tro")
@Data
public class VaiTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_vai_tro")
    private String tenVaiTro;
}

