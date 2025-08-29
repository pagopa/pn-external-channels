package it.pagopa.pn.externalchannels.middleware;

import it.pagopa.pn.api.dto.events.MomProducer;
import it.pagopa.pn.externalchannels.event.OcrEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OcrSendClient {

    private final MomProducer<OcrEvent> ocrProducer;

    public void sendOcr(OcrEvent ocrEvent) {
        ocrProducer.push(ocrEvent);
    }
}
