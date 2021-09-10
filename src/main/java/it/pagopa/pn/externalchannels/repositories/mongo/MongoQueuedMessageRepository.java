package it.pagopa.pn.externalchannels.repositories.mongo;

import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoQueuedMessageRepository extends MongoRepository<QueuedMessage, String> {

    List<QueuedMessage> findByEventStatus(String statoAvviso);

}
