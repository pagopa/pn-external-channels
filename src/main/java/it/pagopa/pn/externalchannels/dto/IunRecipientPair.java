package it.pagopa.pn.externalchannels.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class IunRecipientPair implements Serializable {

    private String iun;
    private String recipient;
}
