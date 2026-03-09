package com.poly.shopquanao.dto.client;

import java.util.Objects;

public class AttributeDTO {

    private Integer id;
    private String ten;   // đổi từ name → ten

    public AttributeDTO(Integer id, String ten) {
        this.id = id;
        this.ten = ten;
    }

    public Integer getId() {
        return id;
    }

    public String getTen() {   // getter phải là getTen()
        return ten;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeDTO)) return false;
        AttributeDTO that = (AttributeDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}