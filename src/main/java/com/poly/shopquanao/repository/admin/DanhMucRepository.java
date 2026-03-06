package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DanhMucRepository
        extends JpaRepository<DanhMuc, Integer> {
}