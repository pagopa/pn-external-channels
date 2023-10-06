package it.pagopa.pn.externalchannels.dto.postelmock;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * MasterOut
 */
@Data
@JsonPropertyOrder({
        MasterOut.JSON_PROPERTY_ID,
        MasterOut.JSON_PROPERTY_N_RISULTATO_NORM,
        MasterOut.JSON_PROPERTY_N_ERRORE_NORM,
        MasterOut.JSON_PROPERTY_S_SIGLA_PROV,
        MasterOut.JSON_PROPERTY_F_POSTALIZZABILE,
        MasterOut.JSON_PROPERTY_S_STATO_UFF,
        MasterOut.JSON_PROPERTY_S_STATO_ABB,
        MasterOut.JSON_PROPERTY_S_STATO_SPEDIZIONE,
        MasterOut.JSON_PROPERTY_S_COMUNE_UFF,
        MasterOut.JSON_PROPERTY_S_COMUNE_ABB,
        MasterOut.JSON_PROPERTY_S_COMUNE_SPEDIZIONE,
        MasterOut.JSON_PROPERTY_S_FRAZIONE_UFF,
        MasterOut.JSON_PROPERTY_S_FRAZIONE_ABB,
        MasterOut.JSON_PROPERTY_S_FRAZIONE_SPEDIZIONE,
        MasterOut.JSON_PROPERTY_S_CIVICO_ALTRO,
        MasterOut.JSON_PROPERTY_S_CAP,
        MasterOut.JSON_PROPERTY_S_PRESSO,
        MasterOut.JSON_PROPERTY_S_VIA_COMPLETA_UFF,
        MasterOut.JSON_PROPERTY_S_VIA_COMPLETA_ABB,
        MasterOut.JSON_PROPERTY_S_VIA_COMPLETA_SPEDIZIONE
})
@JsonTypeName("MasterOut")
public class MasterOut {
    public static final String JSON_PROPERTY_ID = "id";
    private String id;

    public static final String JSON_PROPERTY_N_RISULTATO_NORM = "nRisultatoNorm";
    private Integer nRisultatoNorm;

    public static final String JSON_PROPERTY_N_ERRORE_NORM = "nErroreNorm";
    private Integer nErroreNorm;

    public static final String JSON_PROPERTY_S_SIGLA_PROV = "sSiglaProv";
    private String sSiglaProv;

    public static final String JSON_PROPERTY_F_POSTALIZZABILE = "fPostalizzabile";
    private String fPostalizzabile;

    public static final String JSON_PROPERTY_S_STATO_UFF = "sStatoUff";
    private String sStatoUff;

    public static final String JSON_PROPERTY_S_STATO_ABB = "sStatoAbb";
    private String sStatoAbb;

    public static final String JSON_PROPERTY_S_STATO_SPEDIZIONE = "sStatoSpedizione";
    private String sStatoSpedizione;

    public static final String JSON_PROPERTY_S_COMUNE_UFF = "sComuneUff";
    private String sComuneUff;

    public static final String JSON_PROPERTY_S_COMUNE_ABB = "sComuneAbb";
    private String sComuneAbb;

    public static final String JSON_PROPERTY_S_COMUNE_SPEDIZIONE = "sComuneSpedizione";
    private String sComuneSpedizione;

    public static final String JSON_PROPERTY_S_FRAZIONE_UFF = "sFrazioneUff";
    private String sFrazioneUff;

    public static final String JSON_PROPERTY_S_FRAZIONE_ABB = "sFrazioneAbb";
    private String sFrazioneAbb;

    public static final String JSON_PROPERTY_S_FRAZIONE_SPEDIZIONE = "sFrazioneSpedizione";
    private String sFrazioneSpedizione;

    public static final String JSON_PROPERTY_S_CIVICO_ALTRO = "sCivicoAltro";
    private String sCivicoAltro;

    public static final String JSON_PROPERTY_S_CAP = "sCap";
    private String sCap;

    public static final String JSON_PROPERTY_S_PRESSO = "sPresso";
    private String sPresso;

    public static final String JSON_PROPERTY_S_VIA_COMPLETA_UFF = "sViaCompletaUff";
    private String sViaCompletaUff;

    public static final String JSON_PROPERTY_S_VIA_COMPLETA_ABB = "sViaCompletaAbb";
    private String sViaCompletaAbb;

    public static final String JSON_PROPERTY_S_VIA_COMPLETA_SPEDIZIONE = "sViaCompletaSpedizione";
    private String sViaCompletaSpedizione;
}

