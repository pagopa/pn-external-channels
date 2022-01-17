package it.pagopa.pn.externalchannels.pecbysmtp;

import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.externalchannels.pecbysmtp.jmailutils.JMailStoreWrapper;
import it.pagopa.pn.externalchannels.pecbysmtp.jmailutils.JMailUtils;
import it.pagopa.pn.externalchannels.pecbysmtp.jmailutils.PecEntry;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.service.EventSenderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;

@Service
@Slf4j
public class ReceiverService {

    private final PecBySmtpCfg cfg;
    private final EventSenderService eventEmitter;
    private final JMailUtils jmailUtils;
    private final PecMetadataDao dao;

    @Value("${spring.cloud.stream.bindings." + PnExtChnProcessor.STATUS_OUTPUT + ".destination}")
    private String statusMessageQueue;

    private JMailStoreWrapper store;

    public ReceiverService(PecBySmtpCfg cfg, EventSenderService eventtEmitter, JMailUtils jmailUtils, PecMetadataDao dao) {
        this.cfg = cfg;
        this.eventEmitter = eventtEmitter;
        this.jmailUtils = jmailUtils;
        this.dao = dao;
    }

    protected void renewStore() {
        if(StringUtils.isNotBlank( cfg.getUser() )) {
            if( store != null ) {
                log.info("Close IMAP Store");
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
    }

    private JMailStoreWrapper newConnectedStore() throws MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        props.setProperty("mail.imap.ssl.protocols", "TLSv1.2");

        log.info("Open IMAP Store host={} user={}", cfg.getImapsHost(), cfg.getUser());

        Session session = Session.getInstance(props, null);
        Store storeToBeWrapped = session.getStore( "imap" );
        try {
            storeToBeWrapped.connect( cfg.getImapsHost(), cfg.getUser(), cfg.getPassword());
        }
        catch(MessagingException exc) {
            storeToBeWrapped.close();
        }
        return jmailUtils.wrap(storeToBeWrapped);
    }


    @Scheduled( fixedDelay = 10 * 1000)
    protected void scanForMessages() {
        if(StringUtils.isNotBlank( cfg.getUser() ) && !dao.isEmpty() ) {
            log.info("Start IMAP pec polling on host={} with user={}", cfg.getImapsHost(), cfg.getUser());
            this.renewStore();
            store.listEntries()
                    .stream()
                    .filter( pecEntry -> POSSIBLE_RESPONSE_TYPES.contains( pecEntry.getType() ))
                    .map( this::enrichStreamEntry )
                    .filter( Optional::isPresent )
                    .map( Optional::get )
                    .forEach( entry -> {
                        StandardEventHeader eventheader = entry.getEvt().getHeader();
                        log.info("Generate PEC result event eventId={} iun={} outcome={}",
                                eventheader.getEventId(),
                                eventheader.getIun(),
                                entry.getEvt().getPayload().getStatusCode()
                            );
                        sendAckEvent( entry.getEvt() );
                        dao.remove( entry.getMetadata().getKey() );
                    });
        }
    }

    private Optional<StreamEntry> enrichStreamEntry(PecEntry entry) {
        Optional<SimpleMessage> message = dao.getMessageMetadata( entry.getReferredId() );
        return message.map(
                msg -> new StreamEntry( this.metadataToEvent(msg, entry), msg)
            );
    }

    private static final Collection<PecEntry.Type> POSSIBLE_RESPONSE_TYPES = Arrays.asList(
            PecEntry.Type.DELIVERED_RECIPE,
            PecEntry.Type.DELIVERING_ERROR_RECIPE
        );

    private void sendAckEvent(PnExtChnProgressStatusEvent event) {
        eventEmitter.sendTo( statusMessageQueue, event);
    }

    protected PnExtChnProgressStatusEvent metadataToEvent( SimpleMessage metadata, PecEntry pecEntry ) {

        String iun = metadata.getIun();
        String requestEventId = metadata.getEventId();
        String responseEventId = requestEventId + "_response";

        PecEntry.Type entryType = pecEntry.getType();

        PnExtChnProgressStatus outcome;
        switch( entryType ) {
            case DELIVERED_RECIPE:
                outcome = PnExtChnProgressStatus.OK;
                break;
            case DELIVERING_ERROR_RECIPE:
                outcome = PnExtChnProgressStatus.PERMANENT_FAIL;
                break;
            default:
                throw new PnInternalException("Pec entry type " + entryType + " not mapped to event Type");
        }

        log.debug("Mapping pec entry to external-channel event eventId={} iun={} pecEntryId={}" +
                "pecEntryReferredId={} pecEntryType={} eventOutcome={}",
                requestEventId, pecEntry.getEntryId(), pecEntry.getReferredId(), iun, outcome );

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
                        .statusCode(outcome)
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
