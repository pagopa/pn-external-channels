package it.pagopa.pn.externalchannels.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;

/**
 * Pojo che viene salvato nel database in memory. Tiene traccia dei messaggi (codici) da inviare.
 * Conserva in una queue i codici da mandare {@link #codeToSend} e dopo quanto tempo inviarli {@link #timeToSend}.
 * Le due queue vanno di pari passo (cioè vengono scodate nello stesso momento, quindi l'elemento i-esimo della coda
 * che tiene traccia dei codici da mandare, è associato all'elemento i-esimo della coda che tiene traccia delle durate).
 * <p>
 * Esempio: codeToSend = C000, C001, C005, C003 timeToSend = 5s, 10s, 5s, 6s.
 * Il codice C000 sarà mandato dopo cinque secondi dall'ultimo invio, il codice C001 dopo 10 secondi e così via.
 * <p>
 * La prima volta che dovrà essere mandato un codice, verrà utilizzato il campo {@link #createMessageTimestamp}
 * essendo {@link #lastMessageSentTimestamp} non popolato ancora.
 */
@Data
public class NotificationProgress implements Serializable {

    private String requestId;

    private String destinationAddress;

    private LinkedList<String> codeToSend;

    private LinkedList<Duration> timeToSend;

    private Instant lastMessageSentTimestamp;

    private Instant createMessageTimestamp;

    private String appSourceName;

    private String iun;

    private String channel;

}
