package com.example.tutti.resource.folder;

public enum MarchingType {
    CHURCH_SONGS("Pieśni kościelne"),
    FUNERAL_SONGS("Pieśni pogrzebowe"),
    ENTERTAINMENT("Utwory rozrywkowe"),
    MARCHES("Marsze");

    private final String polishVersion;

    MarchingType(String polishVersion) {
        this.polishVersion = polishVersion;
    }

    public String getPolishVersion() {
        return polishVersion;
    }

}