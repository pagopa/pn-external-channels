package it.pagopa.pn.externalchannels.service;


import com.datastax.oss.driver.api.core.uuid.Uuids;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.entities.discardedmessage.DiscardedMessage;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.entities.resultdescriptor.ResultDescriptor;
import it.pagopa.pn.externalchannels.entities.senderpa.SenderConfigByDenomination;
import it.pagopa.pn.externalchannels.entities.senderpa.SenderPecByDenomination;
import it.pagopa.pn.externalchannels.event.QueuedMessageChannel;
import it.pagopa.pn.externalchannels.event.QueuedMessageStatus;
import it.pagopa.pn.externalchannels.pojos.ElaborationResult;
import it.pagopa.pn.externalchannels.pojos.PnExtChnEvnPec;
import it.pagopa.pn.externalchannels.repositories.cassandra.*;
import it.pagopa.pn.externalchannels.util.Constants;
import it.pagopa.pn.externalchannels.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;
import static it.pagopa.pn.externalchannels.event.QueuedMessageStatus.TO_SEND;

@Service
@Slf4j
@ConditionalOnProperty(name = "dev-options.fake-pn-ext-chn-service", havingValue = "false")
public class PnExtChnServiceImpl implements PnExtChnService {

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	QueuedMessageRepository queuedMessageRepository;

	@Autowired
	DiscardedMessageRepository discardedMessageRepository;

	@Autowired
	ResultDescriptorRepository resultDescriptorRepository;

	@Autowired
	QueueMessagingTemplate statusQueueMessagingTemplate;

	@Autowired
	SenderConfigByDenominationRepository senderConfigByDenominationRepository;

	@Autowired
	SenderPecByDenominationRepository senderPecByDenominationRepository;

	@Value("${spring.cloud.stream.bindings." + PnExtChnProcessor.STATUS_OUTPUT + ".destination}")
	String statusMessageQueue;

	@Override
	public void savePaperMessage(PnExtChnPaperEvent notificaCartacea) {
		log.info("PnExtChnServiceImpl - savePaperMessage - START");
		QueuedMessage queuedMessage = mapBodyToQueuedMessage(notificaCartacea.getPayload());
		setPaPec(queuedMessage);
		setPaConfig(queuedMessage, QueuedMessageChannel.PAPER);
		queuedMessageRepository.save(queuedMessage);
		log.info("PnExtChnServiceImpl - savePaperMessage - END");
	}

	@Override
	public void saveDigitalMessage(PnExtChnPecEvent notificaDigitale) {
		log.info("PnExtChnServiceImpl - saveDigitalMessage - START");
		QueuedMessage queuedMessage = mapBodyToQueuedMessage(notificaDigitale.getPayload());
		setPaConfig(queuedMessage, QueuedMessageChannel.DIGITAL);
		queuedMessageRepository.save(queuedMessage);
		log.info("PnExtChnServiceImpl - saveDigitalMessage - END");
	}

	@Override
	public <T> void discardMessage(String message, Set<ConstraintViolation<T>> violations) {
		log.info("PnExtChnServiceImpl - discardMessage - START");

		List<String> reasons;
		if(violations == null)
			reasons = Collections.singletonList("invalid message");
		else
			reasons = violations.stream()
			.map(v -> v.getPropertyPath() + " " + v.getMessage())
			.collect(Collectors.toList());
		DiscardedMessage discardedMessage = new DiscardedMessage();
		discardedMessage.setId(Uuids.timeBased().toString());
		discardedMessage.setMessage(message);
		discardedMessage.setReasons(reasons);
		discardedMessageRepository.save(discardedMessage);

		log.info("PnExtChnServiceImpl - discardMessage - END");
	}

	private QueuedMessage mapBodyToQueuedMessage(Object body) {
		QueuedMessage m = modelMapper.map(body, QueuedMessage.class);
		m.setId(Uuids.timeBased().toString());
		String actCode = Util
				.lastChars(String.format("%020d", new BigInteger(m.getId().replace("-", ""), 16)), 20);
		m.setActCode(actCode);
		m.setEventStatus(TO_SEND.toString());
		return m;
	}

	private void setPaPec(QueuedMessage qm) {
		SenderPecByDenomination pecInfo = senderPecByDenominationRepository
				.findFirstByDenomination(qm.getSenderDenomination());
		qm.setSenderPecAddress(pecInfo.getPec());
		qm.setSenderId(pecInfo.getIdPec());
	}

	private void setPaConfig(QueuedMessage qm, QueuedMessageChannel channel) {
		String serviceLevel = qm.getServiceLevel();
		if (channel == QueuedMessageChannel.DIGITAL) {
			serviceLevel = Constants.PEC;
			qm.setServiceLevel(serviceLevel);
		}
		SenderConfigByDenomination configInfo = senderConfigByDenominationRepository
				.findByDenominationAndChannelAndServiceLevel(qm.getSenderDenomination(), channel.name(), serviceLevel);

		qm.setTemplate(configInfo.getTemplate());
		qm.setPrintModel(configInfo.getPrintModel());
	}

	@Override
	public void processElaborationResults(List<ElaborationResult> elaborationResultList){
		log.info("PnExtChnServiceImpl - processElaborationResults - START");

		List<ElaborationResult> elaborationResults = elaborationResultList.stream()
				.filter(r -> r != null && StringUtils.isNotBlank(r.getIun()))
				.collect(Collectors.toList());

		List<String> iuns = elaborationResults.stream()
				.map(ElaborationResult::getIun)
				.collect(Collectors.toList());

		List<QueuedMessage> queuedMessages = iuns.stream()
				.map(queuedMessageRepository::findByIun)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		List<ResultDescriptor> resultDescriptors = resultDescriptorRepository.listAll();

		queuedMessages.forEach(qm -> {
			ElaborationResult elabRes = elaborationResults.stream()
					.filter(er -> er.getIun().equals(qm.getIun()))
					.findFirst()
					.orElseGet(ElaborationResult::new);

			Runnable logWarning = () ->
					log.warn("Message with IUN " + qm.getIun() + " has unknown result code: " + elabRes.getResult());

			resultDescriptors.stream()
					.filter(rd -> rd.getCode().equals(elabRes.getResult()))
					.findFirst()
					.ifPresentOrElse(
							rd -> changeStatusAndNotify(qm, elabRes, rd),
							logWarning
					);
		});

		log.info("PnExtChnServiceImpl - processElaborationResults - END");
	}

	private void changeStatusAndNotify(QueuedMessage qm, ElaborationResult er, ResultDescriptor rd) {
		QueuedMessageStatus status = QueuedMessageStatus.OK;
		if (!rd.isPositive()) {
			if (rd.isRetryable())
				status = QueuedMessageStatus.FAILED;
			else
				status = QueuedMessageStatus.ERROR;
		}
		PnExtChnProgressStatus externalStatus = QueuedMessageStatus.toPnExtChnProgressStatus(status);
		qm.setEventStatus(status.name());
		queuedMessageRepository.save(qm);
		produceStatusMessage(qm.getActCode(), qm.getIun(), EventType.SEND_PEC_RESPONSE, externalStatus,
				null, Util.toInt(er.getNotificationAttemptNumber(), 1), null, null);
	}
	
	@Override
	public void produceStatusMessage(String codiceAtto, String iun, EventType tipoInvio, PnExtChnProgressStatus stato, String canale,
									 int tentativo, String codiceRaccomandata, PnExtChnEvnPec pec) {
		log.info("PnExtChnServiceImpl - produceStatusMessage - START");

		PnExtChnProgressStatusEventPayload statusMessage = PnExtChnProgressStatusEventPayload.builder()
				.canale(canale)
				.codiceAtto(codiceAtto)
				.codiceRaccomandata(codiceRaccomandata)
				.iun(iun)
				.tipoInvio(tipoInvio != null ? tipoInvio.name() : "")
				.tentativo(tentativo)
				.statusCode(stato)
				.statusDate(Instant.now())
				.build();

		if(pec != null) {
			statusMessage = statusMessage.toBuilder()
					.iDPec(pec.getIdPec())
					.ricevutaEMLConsegna(pec.getRicevutaEMLConsegna())
					.ricevutaEMLInvio(pec.getRicevutaEMLInvio())
					.build();
		}

		StandardEventHeader stdHeader = builder()
				.iun(iun)
				.eventId("")
				.eventType(tipoInvio != null ? tipoInvio.name() : "")
				.createdAt(Instant.now())
				.publisher(canale)
				.build();

		statusQueueMessagingTemplate.convertAndSend(statusMessageQueue, statusMessage, headersToMap(stdHeader));

		log.info("PnExtChnServiceImpl - produceStatusMessage - END");
	}

	Map<String, Object> headersToMap(StandardEventHeader header) {
		Map<String, String> map = new HashMap<>();
		map.put(PN_EVENT_HEADER_IUN, header.getIun());
		map.put(PN_EVENT_HEADER_EVENT_ID, header.getEventId());
		map.put(PN_EVENT_HEADER_EVENT_TYPE, header.getEventType());
		map.put(PN_EVENT_HEADER_CREATED_AT, Util.formatInstant(header.getCreatedAt()));
		map.put(PN_EVENT_HEADER_PUBLISHER, header.getPublisher());
		map.keySet().stream()
				.filter(k -> StringUtils.isBlank(map.get(k)))
				.collect(Collectors.toList())
				.forEach(map::remove); // null or empty headers cause 500
		return new HashMap<>(map); // cast to Map<String, Object>
	}

}
