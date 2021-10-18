/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.externalchannels.event.elaborationresult.PnExtChnElaborationResultEvent;
import it.pagopa.pn.externalchannels.pojos.ElaborationResult;
import it.pagopa.pn.externalchannels.service.pnextchnservice.PnExtChnService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author GIANGR40
 */
@Service
@Slf4j
@NoArgsConstructor
public class PnExtChnElaborationResultInboundService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PnExtChnFileTransferService fileTransferService;

    @Autowired
    CsvService csvService;

    @Autowired
    PnExtChnService pnExtChnService;

//    @StreamListener(
//            target = PnExtChnProcessor.ELAB_RESULT_INPUT
//    )
    public void handleElaborationResult(PnExtChnElaborationResultEvent evt) throws IOException {
        try {
            log.info("PnExtChnElaborationResultInboundService - handleElaborationResult - START");

            byte[] bytes = fileTransferService.retrieveCsv(evt.getKey());

            List<ElaborationResult> elaborationResults = csvService.csvToElaborationResults(bytes);

            pnExtChnService.processElaborationResults(elaborationResults);

            log.info("PnExtChnElaborationResultInboundService - handleElaborationResult - END");
        } catch(Exception e) {
            log.error("PnExtChnElaborationResultInboundService - handleElaborationResult", e);
            throw e;
        }
    }
}
