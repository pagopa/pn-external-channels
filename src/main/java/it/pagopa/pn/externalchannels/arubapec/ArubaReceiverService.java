package it.pagopa.pn.externalchannels.arubapec;

import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.externalchannels.arubapec.jmailutils.JMailStoreWrapper;
import it.pagopa.pn.externalchannels.arubapec.jmailutils.JMailUtils;
import it.pagopa.pn.externalchannels.arubapec.jmailutils.PecEntry;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.service.EventSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;

@Service
@Slf4j
public class ArubaReceiverService {

    private final ArubaCfg cfg;
    private final EventSenderService eventEmitter;
    private final JMailUtils jmailUtils;
    private final PecMetadataDao dao;

    @Value("${spring.cloud.stream.bindings." + PnExtChnProcessor.STATUS_OUTPUT + ".destination}")
    private String statusMessageQueue;

    private JMailStoreWrapper store;

    public ArubaReceiverService(ArubaCfg cfg, EventSenderService eventtEmitter, JMailUtils jmailUtils, PecMetadataDao dao) {
        this.cfg = cfg;
        this.eventEmitter = eventtEmitter;
        this.jmailUtils = jmailUtils;
        this.dao = dao;
        this.renewStore();
    }

    protected void renewStore() {
        if( store != null ) {
            try {
                store.close();
            } catch (MessagingException exc) {
                log.error("Closing mail store", exc);
            }
        }
        try {
            store = this.newConnectedStore();
        } catch (MessagingException exc) {
            throw new PnInternalException("Connecting to imap", exc);
        }
    }

    private JMailStoreWrapper newConnectedStore() throws MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        props.setProperty("mail.imap.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, null);
        Store store = session.getStore( "imap" );
        store.connect( "imap.pec.it", cfg.getUser(), cfg.getPassword());
        return jmailUtils.wrap(store);
    }


    @Scheduled( fixedDelay = 10 * 1000)
    protected void scanForMessages() {
        store.listEntries()
            .stream()
            .filter( entry -> PecEntry.Type.DELIVERED_RECIPE.equals( entry.getType() ))
            .map( entry -> dao.getMessageMetadata(entry.getReferredId()) )
            .filter( Optional::isPresent )
            .map( Optional::get )
            .map( metadata -> new StreamEntry( this.metadataToEvent(metadata), metadata) )
            .forEach( entry -> {
                log.info("Receive PEC ACK: " + entry.getMetadata());
                sendAckEvent( entry.getEvt() );
                dao.remove( entry.getMetadata().getKey() );
            });
    }

    private void sendAckEvent(PnExtChnProgressStatusEvent event) {
        eventEmitter.sendTo( statusMessageQueue, event);
    }

    protected PnExtChnProgressStatusEvent metadataToEvent( SimpleMessage metadata ) {

        String iun = metadata.getIun();
        String requestEventId = metadata.getEventId();
        String responseEventId = requestEventId + "_response";

        return PnExtChnProgressStatusEvent.builder()
                .header( StandardEventHeader.builder()
                        .createdAt( Instant.now() )
                        .publisher(EventPublisher.EXTERNAL_CHANNELS.name())
                        .eventType( EventType.SEND_PEC_RESPONSE.name())
                        .eventId( responseEventId )
                        .iun( iun )
                        .build()
                )
                .payload( PnExtChnProgressStatusEventPayload.builder()
                        .iun( iun)
                        .canale("PEC")
                        .requestCorrelationId( requestEventId )
                        .statusCode( PnExtChnProgressStatus.OK )
                        .statusDate( Instant.now() )
                        .build()
                )
                .build();
    }


    @lombok.Value
    private static class StreamEntry {
        private PnExtChnProgressStatusEvent evt;
        private SimpleMessage metadata;
    }





}
