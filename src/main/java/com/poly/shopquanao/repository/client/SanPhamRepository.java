package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    @Query("""
        select distinct sp from SanPham sp
        left join fetch sp.chiTietList ct
        left join fetch ct.mauSac
        left join fetch ct.kichCo
        left join fetch sp.hinhAnhList ha
        left join fetch ha.mauSac
        where sp.trangThai = true
    """)
    List<SanPham> findAllWithDetails();

    @Query("""
        select distinct sp from SanPham sp
        left join fetch sp.chiTietList ct
        left join fetch ct.mauSac
        left join fetch ct.kichCo
        left join fetch sp.hinhAnhList ha
        left join fetch ha.mauSac
        where sp.trangThai = true
          and (
                (:mauIds is null or ct.mauSac.id in :mauIds)
            and (:sizeIds is null or ct.kichCo.id in :sizeIds)
            and (
                    :prices is null
                 or ('duoi-300' in :prices and ct.giaBan < 300000)
                 or ('300-500' in :prices and ct.giaBan between 300000 and 500000)
                 or ('tren-500' in :prices and ct.giaBan > 500000)
                )
          )
    """)
    List<SanPham> filterProducts(@Param("mauIds") List<Integer> mauIds,
                                 @Param("sizeIds") List<Integer> sizeIds,
                                 @Param("prices") List<String> prices);
}