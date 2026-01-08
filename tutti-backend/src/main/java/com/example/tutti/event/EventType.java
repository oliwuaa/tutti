package com.example.tutti.event;

public enum EventType {
    CONCERT("Koncert"),
    REHEARSAL("Próba"),
    GIG("Granie"),
    OTHER("Inne");

    private final String polishVersion;

    EventType(String polishVersion) {
        this.polishVersion = polishVersion;
    }

    public String getPolishVersion() {
        return polishVersion;
    }
}
