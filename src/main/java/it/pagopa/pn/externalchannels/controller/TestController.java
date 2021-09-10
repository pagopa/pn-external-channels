package it.pagopa.pn.externalchannels.controller;

import it.pagopa.pn.externalchannels.entities.csvtemplate.CsvTemplate;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.repositories.cassandra.CsvTemplateRepository;
import it.pagopa.pn.externalchannels.repositories.cassandra.QueuedMessageRepository;
import it.pagopa.pn.externalchannels.repositories.mongo.MongoQueuedMessageRepository;
import it.pagopa.pn.externalchannels.service.ScheduledSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test-external-channel")
public class TestController {

    @Autowired
    QueuedMessageRepository queuedMessageRepository;

    @Autowired
    CsvTemplateRepository csvTemplateRepository;

//    @Autowired
//    MongoQueuedMessageRepository mongoQueuedMessageRepository;

    @Autowired
    ScheduledSenderService scheduledSenderService;

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

    /*@GetMapping(path = "/mongo/getQueuedMessage/{id}")
    public QueuedMessage getMongoQueuedMessage(@PathVariable(name = "id") String id){
        return mongoQueuedMessageRepository.findById(id).orElse(null);
    }

    @GetMapping(path = "/mongo")
    public List<QueuedMessage> getMongo(){
        ArrayList<QueuedMessage> queuedMessages = new ArrayList<>();
        mongoQueuedMessageRepository.findAll().forEach(queuedMessages::add);
        return queuedMessages;
    }

    @PostMapping(path = "/mongo/postQueuedMessage")
    public QueuedMessage postMongoQueuedMessage(@RequestBody QueuedMessage queuedMessage){
        return mongoQueuedMessageRepository.save(queuedMessage);
    }

    @GetMapping(path = "/any/clear")
    public void clear(){
        List<QueuedMessage> all = mongoQueuedMessageRepository.findAll();
        mongoQueuedMessageRepository.deleteAll(all);
        Iterable<QueuedMessage> all1 = queuedMessageRepository.findAll();
        queuedMessageRepository.deleteAll(all1);
    }*/

    @GetMapping(path = "/job/trigger")
    public void triggerJob(){
        scheduledSenderService.retrieveAndSendNotifications();
    }

}
