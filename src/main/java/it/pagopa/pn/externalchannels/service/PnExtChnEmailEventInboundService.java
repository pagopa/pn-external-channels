/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.*;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.config.properties.EmailProperties;
import it.pagopa.pn.externalchannels.service.pnextchnservice.PnExtChnService;
import it.pagopa.pn.externalchannels.util.Constants;
import it.pagopa.pn.externalchannels.util.MessageUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.Instant;
import java.util.Set;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;

/**
 *
 * @author GIANGR40
 */
@Service
@Slf4j
@NoArgsConstructor
public class PnExtChnEmailEventInboundService {

    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
	private Validator validator;

    @Autowired
    PnExtChnService pnExtChnService;
    
    @Autowired
    PnExtChnProcessor processor;

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    MessageUtil messageUtil;

    @Autowired
    EmailProperties emailProperties;

    @StreamListener(
            target = PnExtChnProcessor.NOTIF_PEC_INPUT,
            condition = "T(it.pagopa.pn.externalchannels.util.Util).eventTypeIs(headers, T(it.pagopa.pn.api.dto.events.EventType).SEND_COURTESY_EMAIL)"
    )
    public void handlePnExtChnEmailEvent(
            @Header(name = PN_EVENT_HEADER_PUBLISHER) String publisher,
            @Header(name = PN_EVENT_HEADER_EVENT_ID) String eventId,
            @Header(name = PN_EVENT_HEADER_EVENT_TYPE) String eventType,
            @Header(name = PN_EVENT_HEADER_IUN) String iun,
            @Header(name = PN_EVENT_HEADER_CREATED_AT) String createdAt,
            @Payload JsonNode event
    ) throws MessagingException {
        try {

            log.info("PnExtChnEmailEventInboundService - handlePnExtChnEmailEvent - START");

            PnExtChnEmailEvent pnextchnemailevent = PnExtChnEmailEvent.builder()
                    .header(StandardEventHeader.builder()
                            .publisher(publisher)
                            .eventId(eventId)
                            .eventType(eventType)
                            .iun(iun)
                            .createdAt(Instant.parse(createdAt))
                            .build()
                    ).payload(objectMapper.convertValue(event, PnExtChnEmailEventPayload.class))
                    .build();


            Set<ConstraintViolation<PnExtChnEmailEvent>> errors = validator.validate(pnextchnemailevent);

            if (!errors.isEmpty()) {
                log.error(Constants.MSG_ERRORI_DI_VALIDAZIONE);
                // Invio il messaggio di errore su topic dedicato
                pnExtChnService.produceStatusMessage("",
                        pnextchnemailevent
                                .getPayload().getIun(),
                        EventType.SEND_PAPER_RESPONSE, PnExtChnProgressStatus.PERMANENT_FAIL, null, 1, null, null);
                // Salvo il messaggio di scartato su una struttura DB dedicata
                pnExtChnService.discardMessage(event.asText(), errors);
            } else if (emailConfigProvided()){
                String messageBody = messageUtil
                        .prepareMessage(pnextchnemailevent, emailProperties.getContentType());

                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
                helper.setFrom("Piattaforma Notifica <" + emailProperties.getUsername() + ">");
                helper.setTo(pnextchnemailevent.getPayload().getEmailAddress());
                helper.setSubject(MessageUtil.MSG_SUBJECT);
                helper.setText(messageBody, MessageBodyType.HTML.equals(emailProperties.getContentType()));

                javaMailSender.send(mimeMessage);
            } else
                log.warn("Email username and password not provided - can't attempt email sending");

            log.info("PnExtChnEmailEventInboundService - handlePnExtChnEmailEvent - END");
        } catch(Exception e) {
            log.error("PnExtChnEmailEventInboundService - handlePnExtChnEmailEvent", e);
            throw e;
        }
    }

    private boolean emailConfigProvided(){
        return StringUtils.isNotBlank(emailProperties.getUsername()) &&
                StringUtils.isNotBlank(emailProperties.getPassword());
    }
}
