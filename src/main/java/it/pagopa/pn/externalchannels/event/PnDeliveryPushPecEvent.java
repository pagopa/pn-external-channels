package it.pagopa.pn.externalchannels.event;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.model.BaseMessageProgressEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PnDeliveryPushPecEvent implements GenericEvent<StandardEventHeader, BaseMessageProgressEvent> {

    private StandardEventHeader header;
    private BaseMessageProgressEvent payload;
}
