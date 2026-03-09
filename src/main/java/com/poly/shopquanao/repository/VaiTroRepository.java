package com.poly.shopquanao.repository;

import com.poly.shopquanao.entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VaiTroRepository extends JpaRepository<VaiTro, Integer> {

    Optional<VaiTro> findByTenVaiTro(String tenVaiTro);
}