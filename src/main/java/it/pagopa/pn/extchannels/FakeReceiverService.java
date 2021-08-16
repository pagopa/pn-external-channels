package it.pagopa.pn.extchannels;

import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.extchannels.dao.PecRequestMOM;
import it.pagopa.pn.extchannels.dao.PecResponseMOM;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
public class FakeReceiverService {

    private final PecRequestMOM pecRequestMom;
    private final PecResponseMOM pecResponsetMom;

    public FakeReceiverService(PecRequestMOM pecRequestMom, PecResponseMOM pecResponsetMom) {
        this.pecRequestMom = pecRequestMom;
        this.pecResponsetMom = pecResponsetMom;
    }

    @Scheduled(fixedDelay = 100)
    public void scheduleFixedDelayTask() {

        pecRequestMom.poll( Duration.ofSeconds(1), (evt) -> {

            PnExtChnProgressStatusEvent response = computeResponse( evt );
            log.info( "PEC request to {} for IUN {} outcome: {}",
                    evt.getPayload().getPec(),
                    evt.getHeader().getIun(),
                    response.getPayload().getStato()
                );
            pecResponsetMom.push( response );
        });

    }

    private PnExtChnProgressStatusEvent computeResponse(PnExtChnPecEvent evt) {

        String outcome = decideIfOutcomeIsFail( evt ) ? "FAIL" : "OK";
        return buildResponse( evt, outcome );
    }

    private PnExtChnProgressStatusEvent buildResponse(PnExtChnPecEvent evt, String status) {
        return PnExtChnProgressStatusEvent.builder()
                .header( StandardEventHeader.builder()
                        .eventId( evt.getHeader().getEventId() + "_response" )
                        .iun( evt.getHeader().getIun() )
                        .eventType( EventType.SEND_PEC_RESPONSE )
                        .createdAt( Instant.now() )
                        .publisher( "PN_EXTERNAL_CHANNEL" )
                        .build()
                )
                .payload(PnExtChnProgressStatusEventPayload.builder()
                        .canale("PEC")
                        .iun( evt.getHeader().getIun() )
                        .stato( status )
                        .build()
                )
                .build();
    }

    private boolean decideIfOutcomeIsFail( PnExtChnPecEvent evt ) {
        String eventId = evt.getHeader().getEventId();
        String retryNumberPart = eventId.replaceFirst(".*([0-9]+])^", "$1");

        String pecAddress = evt.getPayload().getPec();
        String domainPart = pecAddress.replaceFirst(".*@", "");

        return domainPart.startsWith("fail-both")
                || (domainPart.startsWith("fail-first") && "1".equals( retryNumberPart ));
    }
}
