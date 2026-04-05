package com.poly.shopquanao.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class AddressController {

    @Qualifier("addressRestTemplate")
    private final RestTemplate restTemplate;

    @GetMapping("/provinces")
    public ResponseEntity<String> getProvinces() {
        String url = "https://provinces.open-api.vn/api/v2/p/";
        String response = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wards")
    public ResponseEntity<String> getWardsByProvince(@RequestParam("provinceCode") String provinceCode) {
        String url = "https://provinces.open-api.vn/api/v2/p/" + provinceCode + "?depth=2";
        String response = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(response);
    }
}