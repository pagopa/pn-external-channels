package it.pagopa.pn.externalchannels.mapper;

import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PaperProgressStatusEvent;

public class PaperProgressStatusEventToConsolidatorePaperProgressStatusEvent {

    private PaperProgressStatusEventToConsolidatorePaperProgressStatusEvent(){}

    public static PaperProgressStatusEvent map(it.pagopa.pn.externalchannels.model.PaperProgressStatusEvent input) {
        return SmartMapper.mapToClass(input, PaperProgressStatusEvent.class );
    }
}
