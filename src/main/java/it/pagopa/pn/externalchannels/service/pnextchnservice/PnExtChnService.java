package it.pagopa.pn.externalchannels.service.pnextchnservice;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import it.pagopa.pn.api.dto.events.EventType;
import it.pagopa.pn.api.dto.events.PnExtChnPaperEvent;
import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.pojos.ElaborationResult;
import it.pagopa.pn.externalchannels.pojos.PnExtChnEvnPec;

public interface PnExtChnService {
	
	void savePaperMessage(PnExtChnPaperEvent paperNotification);
	void saveDigitalMessage(PnExtChnPecEvent digitalNotification);
	<T> void discardMessage(String message, Set<ConstraintViolation<T>> violations);

	List<String> getAttachmentKeys(String eventId);

    void processElaborationResults(List<ElaborationResult> elaborationResults);

    void produceStatusMessage(QueuedMessage qm, EventType tipoInvio, PnExtChnProgressStatus stato, String canale,
                              int tentativo, String codiceRaccomandata, PnExtChnEvnPec pec);

	default void produceStatusMessage(String codiceAtto, String iun, EventType tipoInvio, PnExtChnProgressStatus stato, String canale,
									 int tentativo, String codiceRaccomandata, PnExtChnEvnPec pec) {
		QueuedMessage qm = new QueuedMessage();
		qm.setActCode(codiceAtto);
		qm.setIun(iun);
		produceStatusMessage(qm, tipoInvio, stato, canale, tentativo, codiceRaccomandata, pec);
	}

}
