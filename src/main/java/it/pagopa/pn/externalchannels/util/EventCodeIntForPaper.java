package it.pagopa.pn.externalchannels.util;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public enum EventCodeIntForPaper {

    // codici in arrivo da ext-Channel (C) con/senza busta indica se lo stato contiene allegati
    PAPER_001("001"), // Stampato
    PAPER_002("002"), // Disponibile al recapitista
    PAPER_003("003"), // Preso in carico dal recapitista
    PAPER_004("004"), // Consegnata
    PAPER_005("005"), // Mancata consegna
    PAPER_006("006"), // Furto/Smarrimanto/deterioramento
    PAPER_007("007"), // Consegnato Ufficio Postale
    PAPER_008("008"), // Mancata consegna Ufficio Postale
    PAPER_009("009"); // Compiuta giacenza

    private final String value;
    private static final List<String> EVENT_CODES_WITH_ATTACHMENT = List.of("004"); // solo per A/R


    EventCodeIntForPaper(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static boolean isWithAttachment(String enumString) {
        return EVENT_CODES_WITH_ATTACHMENT.contains(enumString);
    }

    public static String getValueFromEnumString(String enumString) {
        return Enum.valueOf(EventCodeIntForPaper.class, enumString).value;
    }

}
