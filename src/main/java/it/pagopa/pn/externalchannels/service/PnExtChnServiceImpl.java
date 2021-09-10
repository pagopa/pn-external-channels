package it.pagopa.pn.externalchannels.service;


import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;

import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatusEventPayload;
import it.pagopa.pn.externalchannels.event.QueuedMessageStatus;
import it.pagopa.pn.externalchannels.event.eventinbound.pnextchncartevent.PnExtChnCartEvent;
import it.pagopa.pn.externalchannels.repositories.cassandra.DiscardedMessageRepository;
import it.pagopa.pn.externalchannels.repositories.cassandra.QueuedMessageRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.entities.discardedmessage.DiscardedMessage;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.event.eventoutbound.PnExtChnEvnPec;
import it.pagopa.pn.externalchannels.repositories.mongo.MongoDiscardedMessageRepository;
import it.pagopa.pn.externalchannels.repositories.mongo.MongoQueuedMessageRepository;
import it.pagopa.pn.externalchannels.util.Constants;
import it.pagopa.pn.externalchannels.util.TypeCanale;
import lombok.extern.slf4j.Slf4j;

import static it.pagopa.pn.externalchannels.event.QueuedMessageStatus.INVIARE;

@Service
@Slf4j
@ConditionalOnProperty(name = "dev-options.fake-pn-ext-chn-service", havingValue = "false")
public class PnExtChnServiceImpl implements PnExtChnService {

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	PnExtChnProcessor processor;

	@Autowired
	QueuedMessageRepository queuedMessageRepository;

	@Autowired
	DiscardedMessageRepository discardedMessageRepository;

	@Override
	public void salvaMessaggioCartaceo(PnExtChnCartEvent notificaCartacea) {
		log.info("ExternalChannelServiceImpl - salvaMessaggioCartaceo - START");
		QueuedMessage queuedMessage = mapBodyToQueuedMessage(notificaCartacea.getPnExtChnCartEventPayload());
		queuedMessageRepository.save(queuedMessage);
		this.produceStatusMessage(queuedMessage.getCodiceAtto(),
				queuedMessage.getIun(),
				null, PnExtChnProgressStatus.OK, TypeCanale.CARTACEO, 1, null, null, null, null);
		log.info("ExternalChannelServiceImpl - salvaMessaggioCartaceo - END");
	}

	@Override
	public void salvaMessaggioDigitale(PnExtChnPecEvent notificaDigitale) {
		log.info("ExternalChannelServiceImpl - salvaMessaggioDigitale - START");

		QueuedMessage queuedMessage = mapBodyToQueuedMessage(notificaDigitale.getPayload());
		queuedMessageRepository.save(queuedMessage);
		this.produceStatusMessage(queuedMessage.getCodiceAtto(),
				queuedMessage.getIun(),
				null, PnExtChnProgressStatus.OK, TypeCanale.PEC, 1, null, null, null, null);

		log.info("ExternalChannelServiceImpl - salvaMessaggioDigitale - END");
	}

	@Override
	public <T> void scartaMessaggio(String message, Set<ConstraintViolation<T>> violations) {
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
		
	}

	private QueuedMessage mapBodyToQueuedMessage(Object body) {
		QueuedMessage m = modelMapper.map(body, QueuedMessage.class);
		m.setId(Uuids.timeBased().toString());
		m.setEventStatus(INVIARE.toString());
		return m;
	}
	
	@Override
	public void produceStatusMessage(String codiceAtto, String iun, String messageType, PnExtChnProgressStatus stato, TypeCanale canale,
									 int tentativo, String codiceRaccomandata, PnExtChnEvnPec pec, String messageId, String partitionKey) {

		PnExtChnProgressStatusEventPayload statusMessage = PnExtChnProgressStatusEventPayload.builder()
				.canale(canale.toString())
				.codiceAtto(codiceAtto)
				.codiceRaccomandata(codiceRaccomandata)
				.iun(iun)
				.tipoInvio(messageType)
				.tentativo(tentativo)
				.statusCode(stato)
				.statusDate(Instant.now())
				// .statusDetails()
				.build();

		if(pec != null) {
			statusMessage.toBuilder()
					.iDPec(pec.getIdPec())
					.ricevutaEMLConsegna(pec.getRicevutaEMLConsegna())
					.ricevutaEMLInvio(pec.getRicevutaEMLInvio());
		}

		processor.statusMessage().send(
				MessageBuilder
				.withPayload(statusMessage)
				.setHeader("partitionKey", Constants.ZERO_INT_VALUE)
				.build()
		);
		
	}

}
