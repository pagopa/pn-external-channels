package it.pagopa.pn.externalchannels.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@Builder
@EqualsAndHashCode
public class EventCodeMapKey implements Serializable {

    private String iun;
    private String recipient;
    private String code;

}
