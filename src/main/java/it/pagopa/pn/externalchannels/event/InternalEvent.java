package it.pagopa.pn.externalchannels.event;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalEvent implements GenericEvent<StandardEventHeader, NotificationProgress> {

    public static final String INTERNAL_EVENT = "INTERNAL_EVENT";

    private StandardEventHeader header;
    private NotificationProgress payload;
}
