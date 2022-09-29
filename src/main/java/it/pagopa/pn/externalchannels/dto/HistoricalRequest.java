package it.pagopa.pn.externalchannels.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalRequest implements Serializable {

    private String iun;

    private String requestId;

    private List<String> codesSent;

    private Instant lastUpdateInCache;
}
