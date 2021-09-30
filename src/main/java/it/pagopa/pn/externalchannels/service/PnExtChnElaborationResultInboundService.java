/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.event.elaborationresult.ElaborationResult;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

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
    ObjectMapper objectMapper;

    @Autowired
    private PnExtChnFileTransferService fileTransferService;

    @StreamListener(
            target = PnExtChnProcessor.ELAB_RESULT_INPUT
    )
    public void handleElaborationResult(
            @Payload JsonNode event
    ) {
        try {
            log.info("PnExtChnElaborationResultInboundService - handleElaborationResult - START");

            ElaborationResult res = objectMapper.convertValue(event, ElaborationResult.class);

            Map<String, String> map = fileTransferService.retrieveElaborationResult(res.getKey());

            log.info("PnExtChnElaborationResultInboundService - handleElaborationResult - END");
        } catch(RuntimeException e) {
            log.error("PnExtChnElaborationResultInboundService - handleElaborationResult", e);
            throw e;
        }
    }
}
