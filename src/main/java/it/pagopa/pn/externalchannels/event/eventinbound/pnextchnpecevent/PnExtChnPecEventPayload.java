/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.event.eventinbound.pnextchnpecevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

/**
 *
 * @author GIANGR40
 */
@Value @Builder
public class PnExtChnPecEventPayload {       
    
    /**
     * The event property names
     */
    public static final String PN_EXTCHN_PEC_EVENT_CALLBACK_URL = "callbackUrl";
    public static final String PN_EXTCHN_PEC_EVENT_CODICE_ATTO = "codiceAtto";
    public static final String PN_EXTCHN_PEC_EVENT_IUN = "iun";
    public static final String PN_EXTCHN_PEC_EVENT_PA_MITTENTE = "paMittente";
    public static final String PN_EXTCHN_PEC_EVENT_PEC_MITTENTE = "pecMittente";
    public static final String PN_EXTCHN_PEC_EVENT_DESTINATARIO = "destinatario";
    public static final String PN_EXTCHN_PEC_EVENT_PEC = "pec";

    
    @JsonProperty(PnExtChnPecEventPayload.PN_EXTCHN_PEC_EVENT_CALLBACK_URL)
    private String callbackUrl;
    
    @JsonProperty(PnExtChnPecEventPayload.PN_EXTCHN_PEC_EVENT_CODICE_ATTO)
    private String codiceAtto;

    @JsonProperty(PnExtChnPecEventPayload.PN_EXTCHN_PEC_EVENT_IUN)
    private String iun;
    
    @JsonProperty(PnExtChnPecEventPayload.PN_EXTCHN_PEC_EVENT_PA_MITTENTE)
    private String paMittente;
    
    @JsonProperty(PnExtChnPecEventPayload.PN_EXTCHN_PEC_EVENT_DESTINATARIO)
    private String destinatario;
    
    @JsonProperty(PnExtChnPecEventPayload.PN_EXTCHN_PEC_EVENT_PEC)
    private String pec;
    
}
