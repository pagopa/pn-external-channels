package it.pagopa.pn.externalchannels.util;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

public enum EventCodeInt {

    // codici interni Delivery-Push (DP)
    DP00("DP00"), // Tentativo reinvio richiesto: codice interno a delivery push che indica una richiesta di ritentativo
    DP10("DP10"), // Scaduto timeout di invio a ext-channel, senza ottenere un evento di risposta OK/KO/RETRY_PROGRESS

    // codici in arrivo da ext-Channel (C) con/senza busta indica se lo stato contiene allegati
    C000("COMUNICAZIONE CON SERVER PEC AVVENUTA"), // COMUNICAZIONE CON SERVER PEC AVVENUTA  (senza busta)
    C001("ACCETTAZIONE"), // StatusPec.ACCETTAZIONE  (con busta)
    C002("NON_ACCETTAZIONE"), // StatusPec.NON_ACCETTAZIONE  (con busta)
    C003("AVVENUTA_CONSEGNA"), // StatusPec.AVVENUTA_CONSEGNA  (con busta)
    C004("ERRORE_CONSEGNA"), // StatusPec.ERRORE_CONSEGNA (con busta)
    C005("PRESA_IN_CARICO"), // StatusPec.PRESA_IN_CARICO  (senza busta)
    C006("RILEVAZIONE_VIRUS"), // StatusPec.RILEVAZIONE_VIRUS (con busta)
    C007("PREAVVISO_ERRORE_CONSEGNA"), // StatusPec.PREAVVISO_ERRORE_CONSEGNA  (senza busta)
    C008("ERRORE_COMUNICAZIONE_SERVER_PEC"), // StatusPec.ERRORE_COMUNICAZIONE_SERVER_PEC  - con retry da parte di PN (senza busta)
    C009("ERRORE_DOMINIO_PEC_NON_VALIDO"), // StatusPec.ERRORE_DOMINIO_PEC_NON_VALIDO - senza retry:  indica un dominio pec non valido; (senza busta)
    C010("ERROR_INVIO_PEC"); // StatusPec.ERROR_INVIO_PEC - con retry da parte di PN: indica un errore generico di invio pec (senza busta)

    private final String value;
    private static final List<String> EVENT_CODES_WITH_ATTACHMENT = List.of("C001", "C002", "C003", "C004", "C006");


    EventCodeInt(String value) {
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
        return Enum.valueOf(EventCodeInt.class, enumString).value;
    }

}
