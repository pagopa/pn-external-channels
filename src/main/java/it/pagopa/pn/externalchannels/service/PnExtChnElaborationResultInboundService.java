/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.event.elaborationresult.ElaborationResult;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;

/**
 *
 * @author GIANGR40
 */
@Service
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class PnExtChnElaborationResultInboundService {


    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Autowired
    private PnExtChnFileTransferService fileTransferService;

    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        objectMapper = jackson2ObjectMapperBuilder.createXmlMapper(false).build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        objectMapper.setDateFormat(df);
        objectMapper.setTimeZone(TimeZone.getTimeZone(UTC));
    }

    @StreamListener(
            target = PnExtChnProcessor.ELAB_RESULT_INPUT
    )
    public void handleElaborationResult(
            @Payload JsonNode event
    ) {
        log.info("PnExtChnElaborationResultInboundService - handleElaborationResult - START");

        ElaborationResult res = objectMapper.convertValue(event, ElaborationResult.class);

        Map<String, String> map = fileTransferService.retrieveElaborationResult(res.getKey());

        log.info("PnExtChnElaborationResultInboundService - handleElaborationResult - END");
    }
}
