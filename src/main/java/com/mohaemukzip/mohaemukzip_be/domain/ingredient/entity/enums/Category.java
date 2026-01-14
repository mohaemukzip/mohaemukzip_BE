package com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums;


public enum Category {
    PROCESSED_DAIRY("가공/유제품"),
    MEAT_EGG("육류/계란"),
    GRAIN_NUT("곡물/견과류"),
    FRUIT("과일"),
    NOODLE("면"),
    BREAD_CAKE("빵/떡"),
    BEVERAGE("음료"),
    VEGETABLE("채소"),
    SEAFOOD("해산물"),
    SEASONING("조미료/양념"),
    SNACK("간식"),
    ETC("기타");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
