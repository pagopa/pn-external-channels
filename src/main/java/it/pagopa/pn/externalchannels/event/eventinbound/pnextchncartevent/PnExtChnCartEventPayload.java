/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.event.eventinbound.pnextchncartevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *
 * @author GIANGR40
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PnExtChnCartEventPayload {

    /**
     * The event property names
     */
    public static final String PN_EXTCHN_CART_EVENT_CALLBACK_URL = "callbackUrl";
    public static final String PN_EXTCHN_CART_EVENT_DOCUMENTO = "documento";
    public static final String PN_EXTCHN_CART_EVENT_MITTENTE = "mittente";
    public static final String PN_EXTCHN_CART_EVENT_DESTINATARIO = "destinatario";

    @NotNull
    @NotEmpty
    @JsonProperty(PnExtChnCartEventPayload.PN_EXTCHN_CART_EVENT_CALLBACK_URL)
    private String urlCallBack;

    @Valid
    @JsonProperty(PnExtChnCartEventPayload.PN_EXTCHN_CART_EVENT_DOCUMENTO)
    private PnExtChnCartEventPayloadDocument documento;

    @NotNull
    @Valid
    @JsonProperty(PnExtChnCartEventPayload.PN_EXTCHN_CART_EVENT_MITTENTE)
    private PnExtChnCartEventPayloadSender mittente;

    @NotNull
    @Valid
    @JsonProperty(PnExtChnCartEventPayload.PN_EXTCHN_CART_EVENT_DESTINATARIO)
    private PnExtChnCartEventPayloadReceiver destinatario;
    
}
