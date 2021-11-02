package it.pagopa.pn.externalchannels.service.pnextchnservice;


import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.pojos.ElaborationResult;
import it.pagopa.pn.externalchannels.pojos.PnExtChnEvnPec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class PnExtChnServiceSelectorProxy implements PnExtChnService {

	@Autowired
	PnExtChnService pnExtChnService;

	@Autowired
	PnExtChnServiceFakeImpl pnExtChnServiceFake;

	@Override
	public void savePaperMessage(PnExtChnPaperEvent paperNotification) {
		if (pnExtChnServiceFake.decideOutcome(paperNotification) == null)
			pnExtChnService.savePaperMessage(paperNotification);
		else
			pnExtChnServiceFake.savePaperMessage(paperNotification);
	}

	@Override
	public void saveDigitalMessage(PnExtChnPecEvent digitalNotification) {
		if (pnExtChnServiceFake.decideOutcome(digitalNotification) == null)
			pnExtChnService.saveDigitalMessage(digitalNotification);
		else
			pnExtChnServiceFake.saveDigitalMessage(digitalNotification);
	}

	@Override
	public <T> void discardMessage(String message, Set<ConstraintViolation<T>> constraintViolations) {
		// fallback to real implementation
		pnExtChnService.discardMessage(message, constraintViolations);
	}

	@Override
	public List<String> getAttachmentKeys(String eventId) {
		// fallback to real implementation
		return pnExtChnService.getAttachmentKeys(eventId);
	}

	@Override
	public void processElaborationResults(List<ElaborationResult> elaborationResults) {
		// fallback to real implementation
		pnExtChnService.processElaborationResults(elaborationResults);
	}

	@Override
	public void produceStatusMessage(QueuedMessage qm, EventType tipoInvio, PnExtChnProgressStatus stato, String canale, int tentativo, String codiceRaccomandata, PnExtChnEvnPec pec) {
		// fallback to real implementation
		pnExtChnService.produceStatusMessage(qm, tipoInvio, stato, canale, tentativo, codiceRaccomandata, pec);
	}
}
