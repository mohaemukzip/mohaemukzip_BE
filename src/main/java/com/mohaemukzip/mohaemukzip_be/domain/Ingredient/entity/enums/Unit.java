package com.mohaemukzip.mohaemukzip_be.domain.Ingredient.entity.enums;

public enum Unit {
    EACH("개"),
    GRAM("g"),
    ML("ml"),
    CAN("캔"),
    PAN("판"),
    PACK("팩"),
    MO("모"),
    BOWL("공기"),
    PIECE("장"),
    BUNCH("송이"),
    BONG("봉");

    private final String label;

    Unit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}