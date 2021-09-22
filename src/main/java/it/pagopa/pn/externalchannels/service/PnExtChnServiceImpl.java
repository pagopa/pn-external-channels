package it.pagopa.pn.externalchannels.service;


import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.awspring.cloud.core.env.ResourceIdResolver;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import it.pagopa.pn.api.dto.events.PnExtChnPaperEvent;
import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatusEventPayload;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.entities.discardedmessage.DiscardedMessage;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.pojos.PnExtChnEvnPec;
import it.pagopa.pn.externalchannels.repositories.cassandra.DiscardedMessageRepository;
import it.pagopa.pn.externalchannels.repositories.cassandra.QueuedMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

	@Value("${spring.cloud.stream.bindings." + PnExtChnProcessor.STATUS_OUTPUT + ".destination}")
	String statusMessageQueue;

	final QueueMessagingTemplate queueMessagingTemplate;

	@Autowired
	public PnExtChnServiceImpl(AmazonSQSAsync sqsClient) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		MappingJackson2MessageConverter jacksonMessageConverter =
				new MappingJackson2MessageConverter();
		jacksonMessageConverter.setSerializedPayloadClass(String.class);
		jacksonMessageConverter.setObjectMapper(objectMapper);
		jacksonMessageConverter.setStrictContentTypeMatch(false);

		queueMessagingTemplate = new QueueMessagingTemplate(sqsClient, (ResourceIdResolver) null, jacksonMessageConverter);
	}

	@Override
	public void savePaperMessage(PnExtChnPaperEvent notificaCartacea) {
		log.info("PnExtChnServiceImpl - savePaperMessage - START");
		QueuedMessage queuedMessage = mapBodyToQueuedMessage(notificaCartacea.getPayload());
		queuedMessageRepository.save(queuedMessage);
		log.info("PnExtChnServiceImpl - savePaperMessage - END");
	}

	@Override
	public void saveDigitalMessage(PnExtChnPecEvent notificaDigitale) {
		log.info("PnExtChnServiceImpl - saveDigitalMessage - START");
		QueuedMessage queuedMessage = mapBodyToQueuedMessage(notificaDigitale.getPayload());
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
		m.setEventStatus(TO_SEND.toString());
		return m;
	}
	
	@Override
	public void produceStatusMessage(String codiceAtto, String iun, String tipoInvio, PnExtChnProgressStatus stato, String canale,
									 int tentativo, String codiceRaccomandata, PnExtChnEvnPec pec) {
		log.info("PnExtChnServiceImpl - produceStatusMessage - START");

		PnExtChnProgressStatusEventPayload statusMessage = PnExtChnProgressStatusEventPayload.builder()
				.canale(canale)
				.codiceAtto(codiceAtto)
				.codiceRaccomandata(codiceRaccomandata)
				.iun(iun)
				.tipoInvio(tipoInvio)
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

		queueMessagingTemplate.convertAndSend(statusMessageQueue, statusMessage);

		log.info("PnExtChnServiceImpl - produceStatusMessage - END");
	}

}
