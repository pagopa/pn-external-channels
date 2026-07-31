package it.pagopa.pn.externalchannels.dto;

import lombok.Data;

@Data
public class ReworkRequest {
    private String iun;
    private String attemptId;
    private String recIndex;
    private String pcRetry;
    private ReworkRequestType requestType;
}
