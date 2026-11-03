package it.pagopa.pn.externalchannels.dto.kafka;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Creditor {
    private String idPA;
    private String companyName;
}
