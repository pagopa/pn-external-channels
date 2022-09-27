package it.pagopa.pn.externalchannels.event;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.model.SingleStatusUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PnDeliveryPushEmailEvent implements GenericEvent<StandardEventHeader, SingleStatusUpdate> {

    private StandardEventHeader header;
    private SingleStatusUpdate payload;
}
