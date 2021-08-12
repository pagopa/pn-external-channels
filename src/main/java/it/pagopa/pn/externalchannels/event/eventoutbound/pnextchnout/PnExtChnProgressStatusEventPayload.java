/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.event.eventoutbound.pnextchnout;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author GIANGR40
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PnExtChnProgressStatusEventPayload {

    /**
     * The event property names
     */
    public static final String PN_EXTCHN_PROGRESSSTATUS_EVENT_CODICE_ATTO = "codiceAtto";
    public static final String PN_EXTCHN_PROGRESSSTATUS_EVENT_IUN = "iun";
    public static final String PN_EXTCHN_PROGRESSSTATUS_EVENT_TIPO_INVIO = "tipoInvio";
    public static final String PN_EXTCHN_PROGRESSSTATUS_EVENT_CODICE_RACCOMANDATA = "codiceRaccomandata";
    public static final String PN_EXTCHN_PROGRESSSTATUS_EVENT_ID_PEC = "iDPec";
    public static final String PN_EXTCHN_PROGRESSSTATUS_EVENT_RICEVUTA_EML_INVIO = "ricevutaEMLInvio";
    public static final String PN_EXTCHN_PROGRESSSTATUS_EVENT_RICEVUTA_EML_CONSEGNA = "ricevutaEMLConsegna";
    public static final String PN_EXTCHN_PROGRESSSTATUS_EVENT_STATO = "stato";
    public static final String PN_EXTCHN_PROGRESSSTATUS_EVENT_DATA_STATO = "dataStato";
    public static final String PN_EXTCHN_PROGRESSSTATUS_EVENT_TENTATIVO = "tentativo";
    public static final String PN_EXTCHN_PROGRESSSTATUS_EVENT_CANALE = "canale";

    @JsonProperty(PnExtChnProgressStatusEventPayload.PN_EXTCHN_PROGRESSSTATUS_EVENT_CODICE_ATTO)
    private String codiceAtto;

    @JsonProperty(PnExtChnProgressStatusEventPayload.PN_EXTCHN_PROGRESSSTATUS_EVENT_IUN)
    private String iun;

    @JsonProperty(PnExtChnProgressStatusEventPayload.PN_EXTCHN_PROGRESSSTATUS_EVENT_TIPO_INVIO)
    private String tipoInvio;

    @JsonProperty(PnExtChnProgressStatusEventPayload.PN_EXTCHN_PROGRESSSTATUS_EVENT_CODICE_RACCOMANDATA)
    private String codiceRaccomandata;

    @JsonProperty(PnExtChnProgressStatusEventPayload.PN_EXTCHN_PROGRESSSTATUS_EVENT_ID_PEC)
    private String iDPec;

    @JsonProperty(PnExtChnProgressStatusEventPayload.PN_EXTCHN_PROGRESSSTATUS_EVENT_RICEVUTA_EML_INVIO)
    private String ricevutaEMLInvio;

    @JsonProperty(PnExtChnProgressStatusEventPayload.PN_EXTCHN_PROGRESSSTATUS_EVENT_RICEVUTA_EML_CONSEGNA)
    private String ricevutaEMLConsegna;

    @JsonProperty(PnExtChnProgressStatusEventPayload.PN_EXTCHN_PROGRESSSTATUS_EVENT_STATO)
    private String stato;
    
    @JsonProperty(PnExtChnProgressStatusEventPayload.PN_EXTCHN_PROGRESSSTATUS_EVENT_DATA_STATO)
    private LocalDateTime dataStato;

    @JsonProperty(PnExtChnProgressStatusEventPayload.PN_EXTCHN_PROGRESSSTATUS_EVENT_TENTATIVO)
    private Integer tentativo;

    @JsonProperty(PnExtChnProgressStatusEventPayload.PN_EXTCHN_PROGRESSSTATUS_EVENT_CANALE)
    private String canale;
        
}
