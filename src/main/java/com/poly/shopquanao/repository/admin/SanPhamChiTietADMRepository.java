package com.poly.shopquanao.repository.admin;

import com.poly.shopquanao.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamChiTietADMRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    boolean existsBySanPham_IdAndKichCo_IdAndMauSac_Id(Integer sanPhamId, Integer kichCoId, Integer mauSacId);

    Optional<SanPhamChiTiet> findBySanPham_IdAndKichCo_IdAndMauSac_Id(Integer sanPhamId,
                                                                      Integer kichCoId,
                                                                      Integer mauSacId);

    List<SanPhamChiTiet> findByTrangThaiTrue();

    @Query(value = """
    SELECT spct.*
    FROM san_pham_chi_tiet spct
    JOIN san_pham sp ON spct.san_pham_id = sp.id
    WHERE spct.trang_thai = 1
      AND spct.so_luong > 0
      AND (
            :keyword IS NULL
            OR sp.ten_san_pham COLLATE Latin1_General_CI_AI LIKE '%' + :keyword + '%'
            OR sp.ma_san_pham COLLATE Latin1_General_CI_AI LIKE '%' + :keyword + '%'
          )
      AND (:mauSacId IS NULL OR spct.mau_sac_id = :mauSacId)
      AND (:kichCoId IS NULL OR spct.kich_co_id = :kichCoId)
      AND (:giaMin IS NULL OR spct.gia_ban >= :giaMin)
      AND (:giaMax IS NULL OR spct.gia_ban <= :giaMax)
    """, nativeQuery = true)
    List<SanPhamChiTiet> searchForPos(@Param("keyword") String keyword,
                                      @Param("mauSacId") Integer mauSacId,
                                      @Param("kichCoId") Integer kichCoId,
                                      @Param("giaMin") BigDecimal giaMin,
                                      @Param("giaMax") BigDecimal giaMax);
}