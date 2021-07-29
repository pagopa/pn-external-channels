/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.event.eventinbound.pnextchnpecevent;

import lombok.Builder;
import lombok.Value;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author GIANGR40
 */
@Value @Builder
public class PnExtChnPecEvent {
    @JsonProperty("header")
    private PnExtChnPecEventHeader pnExtChnPecEventHeader;
    @JsonProperty("payload")
    private PnExtChnPecEventPayload pnExtChnPecEventPayload;
}
