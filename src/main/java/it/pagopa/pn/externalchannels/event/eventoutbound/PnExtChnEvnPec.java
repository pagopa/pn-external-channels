package it.pagopa.pn.externalchannels.event.eventoutbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PnExtChnEvnPec {
	
	private String idPec;
	private String ricevutaEMLInvio;
	private String ricevutaEMLConsegna;

}

