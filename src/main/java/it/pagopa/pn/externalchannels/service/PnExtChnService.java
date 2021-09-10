package it.pagopa.pn.externalchannels.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.externalchannels.event.eventinbound.pnextchncartevent.PnExtChnCartEvent;
import it.pagopa.pn.externalchannels.event.eventoutbound.PnExtChnEvnPec;
import it.pagopa.pn.externalchannels.util.TypeCanale;

public interface PnExtChnService {
	
	public void salvaMessaggioCartaceo(PnExtChnCartEvent notificaCartacea);
	public void salvaMessaggioDigitale(PnExtChnPecEvent notificaDigitale);
	public <T> void scartaMessaggio(String message, Set<ConstraintViolation<T>> violations);

	void produceStatusMessage(String codiceAtto, String iun, String messageType, PnExtChnProgressStatus stato, TypeCanale canale,
							  int tentativo, String codiceRaccomandata, PnExtChnEvnPec pec, String messageId, String partitionKey);
}
