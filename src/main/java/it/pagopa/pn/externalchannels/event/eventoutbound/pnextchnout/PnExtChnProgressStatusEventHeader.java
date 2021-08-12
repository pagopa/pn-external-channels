/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.event.eventoutbound.pnextchnout;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.pn.externalchannels.event.eventoutbound.OutboundMessageType;
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
public class PnExtChnProgressStatusEventHeader {

    /**
     * The event property names
     */

    @JsonProperty(OutboundMessageType.KAFKA_HEADER_PUBLISHER)
    private String publisher;

    @JsonProperty(OutboundMessageType.KAFKA_HEADER_MESSAGEID)
    private String messageId;

    @JsonProperty(OutboundMessageType.KAFKA_HEADER_MESSAGETYPE)
    private String messageType;

    @JsonProperty(OutboundMessageType.KAFKA_HEADER_PARTITIONKEY)
    private String partitionKey;

}
