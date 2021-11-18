package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.api.dto.events.EventType;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.event.QueuedMessageChannel;
import it.pagopa.pn.externalchannels.event.QueuedMessageStatus;
import it.pagopa.pn.externalchannels.pojos.CsvTransformationResult;
import it.pagopa.pn.externalchannels.repositories.cassandra.QueuedMessageRepository;
import it.pagopa.pn.externalchannels.service.pnextchnservice.PnExtChnService;
import it.pagopa.pn.externalchannels.util.Constants;
import it.pagopa.pn.externalchannels.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    PnExtChnService pnExtChnService;

    @Autowired
    QueuedMessageRepository queuedMessageRepository;

    @Scheduled(cron = "${job.cron-expression}")
    public void retrieveAndSendNotifications() {
        log.info("ScheduledSenderService - retrieveAndSendNotifications - START");
        List<QueuedMessage> messages = queuedMessageRepository
                .findByEventStatus(QueuedMessageStatus.TO_SEND.toString());
        Map<QueuedMessageChannel, List<QueuedMessage>> templateGroups = groupByTemplate(messages);
        templateGroups.forEach((template, msges) -> {

            /* Se la lista di messaggi recuperata supera il valore massimo di invio
             *  tronco e preparo un subset di messaggi da inviare
             *  viceversa vengono inviati tutti i messaggi recuperati
             */
            msges = msges.size() >= batchSize ?
                    msges.subList(Constants.ZERO_INT_VALUE, batchSize.intValue()) : msges;

            setState(msges, QueuedMessageStatus.PROCESSING);

            CsvTransformationResult result = csvService.queuedMessagesToCsv(msges);

            setState(result.getDiscardedMessages(), QueuedMessageStatus.DISCARDED);
            result.getDiscardedMessages().forEach(dm -> pnExtChnService.produceStatusMessage(dm.getActCode(),
                    dm.getIun(),
                    EventType.SEND_PEC_REQUEST, PnExtChnProgressStatus.PERMANENT_FAIL, null, 1, null, null));

            if (result.getCsvContent() != null) {
                fileTransferService.transferCsv(result.getCsvContent(), result.getFileName());
                msges.removeAll(result.getDiscardedMessages());

                setState(msges, QueuedMessageStatus.SENT);
            }

        });
        log.info("ScheduledSenderService - retrieveAndSendNotifications - END");
    }

    // STUB
    private Map<QueuedMessageChannel, List<QueuedMessage>> groupByTemplate(List<QueuedMessage> messages){
        log.info("ScheduledSenderService - groupByTemplate - START");
        Map<QueuedMessageChannel, List<QueuedMessage>> map = messages.stream()
                .collect(Collectors.groupingBy(Util::getChannel));
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
