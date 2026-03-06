package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.KichCo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KichCoClientRepository extends JpaRepository<KichCo, Integer> {

    List<KichCo> findByTrangThaiTrue();

}