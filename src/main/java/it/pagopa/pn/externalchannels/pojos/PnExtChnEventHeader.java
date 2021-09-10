/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.pn.externalchannels.event.eventinbound.InboundMessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PnExtChnEventHeader {

  
    @JsonProperty(InboundMessageType.KAFKA_HEADER_MESSAGEID)
    private String messageId;

    @JsonProperty(InboundMessageType.KAFKA_HEADER_PARTITIONKEY)
    private String partitionKey;

}
