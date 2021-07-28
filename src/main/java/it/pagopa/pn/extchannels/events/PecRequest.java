package it.pagopa.pn.extchannels.events;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class PecRequest {

	private String iun;
	private Instant sentDate;
	private String  address;

}
