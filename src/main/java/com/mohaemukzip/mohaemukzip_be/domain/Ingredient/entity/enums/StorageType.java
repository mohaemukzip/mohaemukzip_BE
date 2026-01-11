package com.mohaemukzip.mohaemukzip_be.domain.Ingredient.entity.enums;

public enum StorageType {
    REFRIGERATOR("냉장"),
    FREEZER("냉동"),
    ROOM_TEMPERATURE("상온");

    private final String label;

    StorageType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}