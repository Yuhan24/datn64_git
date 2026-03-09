package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.KichCo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KichCoRepository extends JpaRepository<KichCo,Integer> {
    List<KichCo> findByTrangThai(Boolean trangThai);
    boolean existsByTenKichCoIgnoreCase(String tenkichCo);
}
