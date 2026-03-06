package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MauSacClientRepository extends JpaRepository<MauSac, Integer> {

    List<MauSac> findByTrangThaiTrue();

}