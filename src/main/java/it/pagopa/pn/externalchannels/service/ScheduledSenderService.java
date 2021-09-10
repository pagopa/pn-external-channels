package it.pagopa.pn.externalchannels.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.externalchannels.event.QueuedMessageStatus;
import it.pagopa.pn.externalchannels.repositories.cassandra.QueuedMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.pojos.CsvTransformationResult;
import it.pagopa.pn.externalchannels.repositories.mongo.MongoQueuedMessageRepository;
import it.pagopa.pn.externalchannels.util.Constants;
import it.pagopa.pn.externalchannels.util.TypeCanale;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScheduledSenderService {

    @Value("${job.batch-size}")
    private Long batchSize;

    @Autowired
    PnExtChnFileTransferService fileTransferService;

    @Autowired
    CsvService csvService;
    
    @Autowired
    PnExtChnProcessor processor;
    
    @Autowired
    PnExtChnService pnExtChnService;

    @Autowired
    QueuedMessageRepository queuedMessageRepository;

    @Scheduled(cron = "${job.cron-expression}")
    public void retrieveAndSendNotifications() {
        log.info("ScheduledSenderService - retrieveAndSendNotifications - START");
        List<QueuedMessage> messages = queuedMessageRepository
                .findByEventStatus(Constants.STATO_INVIARE);
        Map<String, List<QueuedMessage>> templateGroups = groupByTemplate(messages);
        templateGroups.forEach((template, msges) -> {
            
    	/* Se la lista di messaggi recuperata supera il valore massimo di invio
    	 *  tronco e preparo un subset di messaggi da inviare
    	 *  viceversa vengono inviati tutti i messaggi recuperati
         */
    	msges = msges.size() >= batchSize ?
    			msges.subList(Constants.ZERO_INT_VALUE, batchSize.intValue()) : msges;
    	
    	setState(msges, QueuedMessageStatus.PROCESSATO);

        CsvTransformationResult result = csvService.queuedMessagesToCsv(msges);

        setState(result.getDiscardedMessages(), QueuedMessageStatus.SCARTATO);
        result.getDiscardedMessages().forEach(dm -> pnExtChnService.produceStatusMessage(dm.getCodiceAtto(),
        		dm.getIun(),
        		null, PnExtChnProgressStatus.PERMANENT_FAIL, TypeCanale.PEC, 1, null, null, null, null));

        if (result.getCsvContent() != null) {
        	fileTransferService.transferCsv(result.getCsvContent());
        	msges.removeAll(result.getDiscardedMessages());

        	msges.forEach(m -> pnExtChnService.produceStatusMessage(m.getCodiceAtto(),
        			m.getIun(),
        			null, PnExtChnProgressStatus.OK, TypeCanale.PEC, 1, null, null, null, null));

        	setState(msges, QueuedMessageStatus.INVIATO);
        }
            
        });
        log.info("ScheduledSenderService - retrieveAndSendNotifications - END");
    }

    // STUB
    private Map<String, List<QueuedMessage>> groupByTemplate(List<QueuedMessage> messages){
        log.info("ScheduledSenderService - groupByTemplate - START");
        HashMap<String, List<QueuedMessage>> map = new HashMap<>();
        map.put("A", messages);
        log.info("ScheduledSenderService - groupByTemplate - END");
        return map;
    }

    // TODO: migliorare salvataggio stato
    private void setState(List<QueuedMessage> messages, QueuedMessageStatus status) {
        if (messages != null && !messages.isEmpty()) {
            messages.forEach(m -> m.setEventStatus(status.toString()));
            queuedMessageRepository.saveAll(messages);
        }
    }

}
