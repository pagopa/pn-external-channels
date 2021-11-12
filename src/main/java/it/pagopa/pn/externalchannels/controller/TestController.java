package it.pagopa.pn.externalchannels.controller;

import it.pagopa.pn.externalchannels.entities.csvtemplate.CsvTemplate;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.entities.resultdescriptor.ResultDescriptor;
import it.pagopa.pn.externalchannels.entities.senderpa.SenderConfigByDenomination;
import it.pagopa.pn.externalchannels.entities.senderpa.SenderPecByDenomination;
import it.pagopa.pn.externalchannels.repositories.cassandra.*;
import it.pagopa.pn.externalchannels.repositories.mongo.MongoQueuedMessageRepository;
import it.pagopa.pn.externalchannels.service.ScheduledSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/external-channel/test")
//@RequestMapping("/test-external-channel")
public class TestController {

    @Autowired
    QueuedMessageRepository queuedMessageRepository;

    @Autowired
    DiscardedMessageRepository discardedMessageRepository;

    @Autowired
    CsvTemplateRepository csvTemplateRepository;

    @Autowired
    ResultDescriptorRepository resultDescriptorRepository;

    @Autowired
    ScheduledSenderService scheduledSenderService;

    @Autowired
    SenderConfigByDenominationRepository senderConfigByDenominationRepository;

    @Autowired
    SenderPecByDenominationRepository senderPecByDenominationRepository;

    @GetMapping(path = "/cassandra/getQueuedMessage/{id}")
    public QueuedMessage getCassandraQueuedMessage(@PathVariable(name = "id") String id){
        return queuedMessageRepository.findById(id).orElse(null);
    }

    @GetMapping(path = "/cassandra")
    public List<QueuedMessage> getCassandra(){
        ArrayList<QueuedMessage> queuedMessages = new ArrayList<>();
        queuedMessageRepository.findAll().forEach(queuedMessages::add);
        return queuedMessages;
    }

    @PostMapping(path = "/cassandra/postQueuedMessage")
    public QueuedMessage postCassandraQueuedMessage(@RequestBody QueuedMessage queuedMessage){
        return queuedMessageRepository.save(queuedMessage);
    }

    @PostMapping(path = "/cassandra/postCsvTemplate")
    public CsvTemplate postCassandraQueuedMessage(@RequestBody CsvTemplate csvTemplate){
        return csvTemplateRepository.save(csvTemplate);
    }

    @PostMapping(path = "/cassandra/postResultDescriptors")
    public List<ResultDescriptor> postResultDescriptors(@RequestBody List<ResultDescriptor> resultDescriptors){
        Iterable<ResultDescriptor> res = resultDescriptorRepository.saveAll(resultDescriptors);
        List<ResultDescriptor> result = new ArrayList<>();
        res.forEach(result::add);
        return result;
    }

    @PostMapping(path = "/cassandra/postPaPecs")
    public List<SenderPecByDenomination> postPaPecs(@RequestBody List<SenderPecByDenomination> list){
        Iterable<SenderPecByDenomination> res = senderPecByDenominationRepository.saveAll(list);
        List<SenderPecByDenomination> result = new ArrayList<>();
        res.forEach(result::add);
        return result;
    }

    @PostMapping(path = "/cassandra/postPaConfigs")
    public List<SenderConfigByDenomination> postPaConfigs(@RequestBody List<SenderConfigByDenomination> list){
        Iterable<SenderConfigByDenomination> res = senderConfigByDenominationRepository.saveAll(list);
        List<SenderConfigByDenomination> result = new ArrayList<>();
        res.forEach(result::add);
        return result;
    }

    @GetMapping(path = "/any/clear")
    public void clear(){
        Iterable<QueuedMessage> all1 = queuedMessageRepository.findAll();
        queuedMessageRepository.deleteAll(all1);
        queuedMessageRepository.deleteAll();
        discardedMessageRepository.deleteAll(discardedMessageRepository.findAll());
        discardedMessageRepository.deleteAll();
    }

    @GetMapping(path = "/job/trigger")
    public void triggerJob(){
        scheduledSenderService.retrieveAndSendNotifications();
    }

}
