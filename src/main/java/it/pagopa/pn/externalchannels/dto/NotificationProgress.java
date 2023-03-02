package it.pagopa.pn.externalchannels.dto;

import it.pagopa.pn.externalchannels.model.DiscoveredAddress;
import lombok.Data;

import java.time.Instant;
import java.util.LinkedList;

/**
 * Pojo che viene salvato nel database in memory. Tiene traccia dei messaggi (codici) da inviare.
 * Conserva in una queue i codici da mandare e dopo quanto tempo inviarli {@link #codeTimeToSendQueue}.
 * La coppia codice-tempo è confezionata nella classe @{@link CodeTimeToSend}.
 * <p>
 * Esempio: codeTimeToSendQueue = C000,5s C001,10s C005,5s C003,6s.
 * Il codice C000 sarà mandato dopo cinque secondi dall'ultimo invio, poi il codice C001 dopo 10 secondi e così via.
 * <p>
 * La prima volta che dovrà essere mandato un codice, verrà utilizzato il campo {@link #createMessageTimestamp}
 * essendo {@link #lastMessageSentTimestamp} non popolato ancora.
 */
@Data
public class NotificationProgress {

    public enum PROGRESS_OUTPUT_CHANNEL{
        QUEUE_DELIVERY_PUSH,
        QUEUE_PAPER_CHANNEL,
        WEBHOOK_EXT_CHANNEL
    }

    private PROGRESS_OUTPUT_CHANNEL output;

    private String requestId;

    private String destinationAddress;

    private LinkedList<CodeTimeToSend> codeTimeToSendQueue;

    private Instant lastMessageSentTimestamp;

    private Instant createMessageTimestamp;

    private String iun;

    private String channel;

    private DiscoveredAddress discoveredAddress; // per notifica cartacea (esito negativo con indagine postino)

}
