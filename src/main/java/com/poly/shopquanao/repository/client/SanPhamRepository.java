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
        select distinct sp
        from SanPham sp
        left join fetch sp.chiTietList ct
        left join fetch ct.mauSac
        left join fetch ct.kichCo
        left join fetch sp.hinhAnhList ha
        left join fetch ha.mauSac
        where sp.trangThai = true

          and (
                :price is null
                or exists (
                    select 1
                    from SanPhamChiTiet ctPrice
                    where ctPrice.sanPham = sp
                      and (
                            (:price = 'duoi-300' and ctPrice.giaBan < 300000)
                         or (:price = '300-500' and ctPrice.giaBan between 300000 and 500000)
                         or (:price = 'tren-500' and ctPrice.giaBan > 500000)
                      )
                )
          )

          and (
                :mauIds is null
                or exists (
                    select 1
                    from SanPhamChiTiet ctMau
                    where ctMau.sanPham = sp
                      and ctMau.mauSac.id in :mauIds
                )
          )

          and (
                :sizeCount = 0
                or sp.id in (
                    select ctSize.sanPham.id
                    from SanPhamChiTiet ctSize
                    where ctSize.kichCo.id in :sizeIds
                    group by ctSize.sanPham.id
                    having count(distinct ctSize.kichCo.id) = :sizeCount
                )
          )
    """)
    List<SanPham> filterProductsMixed(@Param("mauIds") List<Integer> mauIds,
                                      @Param("sizeIds") List<Integer> sizeIds,
                                      @Param("sizeCount") long sizeCount,
                                      @Param("price") String price);
}