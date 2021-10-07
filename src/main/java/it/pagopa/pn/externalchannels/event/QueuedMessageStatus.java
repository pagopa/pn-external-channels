package it.pagopa.pn.externalchannels.event;

import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;

import java.util.EnumMap;
import java.util.Map;

public enum QueuedMessageStatus {
    OK,
    SENT,
    FAILED,
    ERROR,
    VALIDATION_ERROR,
    DISCARDED,
    VALIDATED,
    TO_SEND,
    PROCESSING;

    private static final Map<QueuedMessageStatus, PnExtChnProgressStatus> converter;

    static {
        converter = new EnumMap<>(QueuedMessageStatus.class);
        converter.put(OK, PnExtChnProgressStatus.OK);
        converter.put(FAILED, PnExtChnProgressStatus.RETRYABLE_FAIL);
        converter.put(ERROR, PnExtChnProgressStatus.PERMANENT_FAIL);
        converter.put(VALIDATION_ERROR, PnExtChnProgressStatus.PERMANENT_FAIL);
    }

    public static PnExtChnProgressStatus toPnExtChnProgressStatus(QueuedMessageStatus ms){
        return converter.getOrDefault(ms, null);
    }
}
