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
            log.info( "PEC request to {} for IUN {} outcome: {} (evt {})",
                    evt.getPayload().getPecAddress(),
                    evt.getHeader().getIun(),
                    response.getPayload().getStatusCode(),
                    evt.getHeader().getEventId()
                );
            pecResponsetMom.push( response );
        });

    }

    private PnExtChnProgressStatusEvent computeResponse(PnExtChnPecEvent evt) {

        PnExtChnProgressStatus outcome = decideOutcome( evt );
        return buildResponse( evt, outcome );
    }

    private PnExtChnProgressStatusEvent buildResponse(PnExtChnPecEvent evt, PnExtChnProgressStatus status) {
        return PnExtChnProgressStatusEvent.builder()
                .header( StandardEventHeader.builder()
                        .eventId( evt.getHeader().getEventId() + "_response" )
                        .iun( evt.getHeader().getIun() )
                        .eventType( EventType.SEND_PEC_RESPONSE.name() )
                        .createdAt( Instant.now() )
                        .publisher( EventPublisher.EXTERNAL_CHANNELS.name() )
                        .build()
                )
                .payload(PnExtChnProgressStatusEventPayload.builder()
                        .canale("PEC")
                        .requestCorrelationId( evt.getPayload().getRequestCorrelationId() )
                        .iun( evt.getHeader().getIun() )
                        .statusCode( status )
                        .statusDate( Instant.now() )
                        .build()
                )
                .build();
    }

    private PnExtChnProgressStatus decideOutcome( PnExtChnPecEvent evt ) {
        String eventId = evt.getHeader().getEventId();
        String retryNumberPart = eventId.replaceFirst(".*([0-9]+)$", "$1");

        String pecAddress = evt.getPayload().getPecAddress();
        String domainPart = pecAddress.replaceFirst(".*@", "");

        PnExtChnProgressStatus status;
        if( domainPart.startsWith("fail-both")
                || (domainPart.startsWith("fail-first") && "1".equals( retryNumberPart )) ) {
            status = PnExtChnProgressStatus.RETRYABLE_FAIL;
        }
        else if( domainPart.startsWith("do-not-exists") ) {
            status = PnExtChnProgressStatus.PERMANENT_FAIL;
        }
        else {
            status = PnExtChnProgressStatus.OK;
        }

        return status;
    }
}
