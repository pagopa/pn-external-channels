package it.pagopa.pn.externalchannels.dto.safestorage;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class FileDownloadInfoInt {
    private String url;
    private BigDecimal retryAfter;
}
