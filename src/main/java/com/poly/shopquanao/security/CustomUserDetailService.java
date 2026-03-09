package com.poly.shopquanao.security;

import com.poly.shopquanao.repository.KhachHangLoginRepository;
import com.poly.shopquanao.repository.NhanVienLoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // ⭐ PHẢI CÓ
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final NhanVienLoginRepository nhanVienRepo;
    private final KhachHangLoginRepository khachHangRepo;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // ADMIN / NHÂN VIÊN
        var nvOpt = nhanVienRepo.findByTenDangNhap(username);
        if (nvOpt.isPresent()) {

            var nv = nvOpt.get();

            return new User(
                    nv.getTenDangNhap(),
                    nv.getMatKhau(),
                    List.of(
                            new SimpleGrantedAuthority("ROLE_" + nv.getVaiTro().getTenVaiTro()
                            )
                    )
            );
        }

        // USER / KHÁCH HÀNG
        var khOpt = khachHangRepo.findByTenDangNhap(username);
        if (khOpt.isPresent()) {

            var kh = khOpt.get();

            return new User(
                    kh.getTenDangNhap(),
                    kh.getMatKhau(),
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }

        throw new UsernameNotFoundException("Không tìm thấy user");
    }
}