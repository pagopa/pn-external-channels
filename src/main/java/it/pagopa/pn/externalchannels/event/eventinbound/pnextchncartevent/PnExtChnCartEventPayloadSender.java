/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.event.eventinbound.pnextchncartevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *
 * @author GIANGR40
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PnExtChnCartEventPayloadSender {
    
    /**
     * The event property names
     */
    public static final String PN_EXTCHN_CART_EVENT_PA_MITTENTE = "paMittente";
    public static final String PN_EXTCHN_CART_EVENT_CART_MITTENTE = "pecMittente";

    @NotNull
    @NotEmpty
    @JsonProperty(PnExtChnCartEventPayloadSender.PN_EXTCHN_CART_EVENT_PA_MITTENTE)
    private String paMittente;

    @NotNull
    @NotEmpty
    @JsonProperty(PnExtChnCartEventPayloadSender.PN_EXTCHN_CART_EVENT_CART_MITTENTE)
    private String pecMittente;
    
}
