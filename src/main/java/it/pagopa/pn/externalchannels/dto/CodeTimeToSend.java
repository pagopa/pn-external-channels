package it.pagopa.pn.externalchannels.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeTimeToSend implements Serializable {

    private String code;
    private Duration time;
}
