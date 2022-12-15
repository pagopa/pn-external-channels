package it.pagopa.pn.externalchannels.util;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public enum EventCodeIntForPaper {

    // codici in arrivo da ext-Channel (C) con/senza busta indica se lo stato contiene allegati
    PAPER_001("__001__"), // Stampato
    PAPER_002("__002__"), // Disponibile al recapitista
    PAPER_003("__003__"), // Preso in carico dal recapitista
    PAPER_004("__004__"), // Consegnata
    PAPER_005("__005__"), // Mancata consegna
    PAPER_006("__006__"), // Furto/Smarrimanto/deterioramento
    PAPER_007("__007__"), // Consegnato Ufficio Postale
    PAPER_008("__008__"), // Mancata consegna Ufficio Postale
    PAPER_009("__009__"); // Compiuta giacenza

    private final String value;
    private static final List<String> EVENT_CODES_WITH_ATTACHMENT = List.of("__004__"); // solo per A/R


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
