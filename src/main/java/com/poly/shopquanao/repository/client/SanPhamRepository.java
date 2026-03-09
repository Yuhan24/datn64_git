package com.poly.shopquanao.repository.client;

import com.poly.shopquanao.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {

    @Query("""
        select distinct sp
        from SanPham sp
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
                      and ctPrice.trangThai = true
                      and (
                            (:price = 'duoi-300' and ctPrice.giaBan < 300000)
                         or (:price = '300-500' and ctPrice.giaBan between 300000 and 500000)
                         or (:price = 'tren-500' and ctPrice.giaBan > 500000)
                      )
                )
          )

          and (
                :mauCount = 0
                or :mauCount = (
                    select count(distinct ctMau.mauSac.id)
                    from SanPhamChiTiet ctMau
                    where ctMau.sanPham = sp
                      and ctMau.trangThai = true
                      and ctMau.mauSac.id in :mauIds
                )
          )

          and (
                :sizeCount = 0
                or :sizeCount = (
                    select count(distinct ctSize.kichCo.id)
                    from SanPhamChiTiet ctSize
                    where ctSize.sanPham = sp
                      and ctSize.trangThai = true
                      and ctSize.kichCo.id in :sizeIds
                )
          )
    """)
    List<SanPham> filterProductsMixed(@Param("mauIds") List<Integer> mauIds,
                                      @Param("mauCount") long mauCount,
                                      @Param("sizeIds") List<Integer> sizeIds,
                                      @Param("sizeCount") long sizeCount,
                                      @Param("price") String price);

    @Query("""
        select distinct sp
        from SanPham sp
        left join fetch sp.chiTietList ct
        left join fetch ct.mauSac
        left join fetch ct.kichCo
        left join fetch sp.hinhAnhList ha
        left join fetch ha.mauSac
        where sp.id = :id
          and sp.trangThai = true
    """)
    Optional<SanPham> findDetailById(@Param("id") Integer id);
}