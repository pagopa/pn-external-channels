/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.event.eventinbound.pnextchnpecevent;

import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
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
public class PnExtChnPecEvent {
    @JsonProperty("header")
    private PnExtChnPecEventHeader pnExtChnPecEventHeader;
    @JsonProperty("payload")
    private PnExtChnPecEventPayload pnExtChnPecEventPayload;
}
