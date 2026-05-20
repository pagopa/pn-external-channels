package it.pagopa.pn.externalchannels.dto;

import lombok.Data;

@Data
public class ReworkRequest {
    private String iun;
    private String attempt;
    private String recIndex;
    private String pcRetry;
    private ReworkRequestType requestType;
}
