package it.pagopa.pn.externalchannels.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import it.pagopa.pn.api.dto.events.EventType;
import it.pagopa.pn.api.dto.events.PnExtChnPaperEvent;
import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.externalchannels.pojos.PnExtChnEvnPec;

public interface PnExtChnService {
	
	void savePaperMessage(PnExtChnPaperEvent notificaCartacea);
	void saveDigitalMessage(PnExtChnPecEvent notificaDigitale);
	<T> void discardMessage(String message, Set<ConstraintViolation<T>> violations);

	void produceStatusMessage(String codiceAtto, String iun, EventType tipoInvio, PnExtChnProgressStatus stato, String canale,
							  int tentativo, String codiceRaccomandata, PnExtChnEvnPec pec);
}
