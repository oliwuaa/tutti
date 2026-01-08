package com.example.tutti.resource.instrument;

public enum InstrumentType {
    PICCOLO("Piccolo"),
    FLUTE("Flet poprzeczny"),
    OBOE("Obój"),
    ENGLISH_HORN("Rożek angielski"),
    FAGOTT("Fagot"),
    CLARINET("Klarnet"),
    ALTO_CLARINET("Klarnet altowy"),
    BASS_CLARINET("Klarnet basowy"),
    SOPRANO_SAX("Saksofon sopranowy"),
    ALTO_SAX("Saksofon altowy"),
    TENOR_SAX("Saksofon Tenorowy"),
    BARITONE_SAX("Saksofon barytonowy"),
    TRUMPET("Trąbka"),
    CORNET("Cornet"),
    FLUGEL_HORN("Skrzydłówka"),
    FRENCH_HORN("Waltornia"),
    TROMBONE("Puzon"),
    EUPHONIUM("Eufonium"),
    TUBA("Tuba"),
    DRUM_KIT("Perkusja"),
    CYMBALS("Talerze"),
    BELLS("Dzwonki"),
    AUX_PERCUSSION("Perkusonalia"),
    KEYBOARD("Klawisze"),
    BASS_GUITAR("Gitara basowa"),
    ELECTRIC_GUITAR("Gitara elektryczna"),
    NONE("Brak");

    private final String polishVersion;

    InstrumentType(String polishVersion) {
        this.polishVersion = polishVersion;
    }

    public String getPolishVersion() {
        return polishVersion;
    }

}
