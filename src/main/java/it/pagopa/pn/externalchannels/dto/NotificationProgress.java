package it.pagopa.pn.externalchannels.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;

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
