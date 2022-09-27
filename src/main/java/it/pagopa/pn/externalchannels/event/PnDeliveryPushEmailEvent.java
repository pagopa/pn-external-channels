package it.pagopa.pn.externalchannels.event;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PnDeliveryPushEmailEvent implements GenericEvent<StandardEventHeader, NotificationProgress> {

    private StandardEventHeader header;
    private NotificationProgress payload;
}
