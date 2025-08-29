package it.pagopa.pn.externalchannels.event;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.GenericEventHeader;
import it.pagopa.pn.externalchannels.dto.OcrOutputMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrEvent implements GenericEvent<GenericEventHeader, OcrOutputMessage> {

    private GenericEventHeader header;
    private OcrOutputMessage payload;
}
