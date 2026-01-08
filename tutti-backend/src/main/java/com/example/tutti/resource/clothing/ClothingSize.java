package com.example.tutti.resource.clothing;

public enum ClothingSize {
    XS(null),
    S(null),
    M(null),
    L(null),
    XL(null),
    SIZE_32(32),
    SIZE_34(34),
    SIZE_36(36),
    SIZE_38(38),
    SIZE_40(40),
    SIZE_42(42),
    SIZE_44(44),
    SIZE_46(46);

    private final Integer number;

    ClothingSize(Integer number) {
        this.number = number;
    }

    public Integer getNumber() {
        return number;
    }
}
