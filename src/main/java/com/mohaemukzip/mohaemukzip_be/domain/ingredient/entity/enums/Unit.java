package com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums;

import lombok.Getter;

@Getter
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
    BONG("봉"),
    MARI("마리"),
    AL("알"),
    STALK("대"),
    HEAD("통"),
    POGI("포기"),
    BUNDLE("단"),
    SLICE("조각");

    private final String label;

    Unit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}