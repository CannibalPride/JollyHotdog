package com.jolly_hotdogs.jollyhotdog;

public enum ItemType {
    DRINK("Drink"),
    SIDES("Sides"),
    MINI_DOG("Mini Dog"),
    HOTDOG("Hotdog"),
    HOTDOG_SANDWICH("Hotdog Sandwich");

    private final String displayName;

    ItemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
