package com.example.tutti.resource.clothing;

public enum ClothingType {
    SHIRT("Koszula"),
    POLO("Polo"),
    COAT("Płaszcz"),
    BAG("Torebka");

    private final String polishVersion;

    ClothingType(String polishVersion) {
        this.polishVersion = polishVersion;
    }

    public String getPolishVersion() {
        return polishVersion;
    }
}
