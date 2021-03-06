package it.pagopa.pn.externalchannels.util;


import it.pagopa.pn.api.dto.events.EventType;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.event.QueuedMessageChannel;
import org.springframework.messaging.MessageHeaders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.PN_EVENT_HEADER_EVENT_TYPE;

public class Util {

    private Util(){}

    public static QueuedMessageChannel getChannel(QueuedMessage qm) {
        return Constants.PEC.equals(qm.getServiceLevel()) ?
                QueuedMessageChannel.DIGITAL : QueuedMessageChannel.PAPER;
    }

    public static String formatInstant(Instant i){
        if (i != null)
            return i.truncatedTo(ChronoUnit.MILLIS).toString();
        else
            return "";
    }

    public static boolean eventTypeIs(MessageHeaders headers, EventType eventType){
        String et = (String) headers.getOrDefault(PN_EVENT_HEADER_EVENT_TYPE, "");
        return eventType.name().equals(et);
    }

    public static boolean eventTypeIsAny(MessageHeaders headers, EventType... eventTypes){
        String et = (String) headers.getOrDefault(PN_EVENT_HEADER_EVENT_TYPE, "");
        return Arrays.stream(eventTypes).anyMatch(evtType -> evtType.name().equals(et));
    }

    public static boolean eventTypeIsKnown(MessageHeaders headers){
        return eventTypeIsAny(headers, EventType.SEND_PAPER_REQUEST, EventType.SEND_PEC_REQUEST, EventType.SEND_COURTESY_EMAIL);
    }

    public static int toInt(String number, int defaultInt){
        try {
            return Integer.parseInt(number.trim());
        } catch (RuntimeException e) {
            return defaultInt;
        }
    }

    public static String lastChars(String s, int num){
        if(s == null || s.length() <= num)
            return s;
        return s.substring(s.length() - num, s.length());
    }

}
