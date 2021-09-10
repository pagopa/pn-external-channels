package it.pagopa.pn.externalchannels.repositories.mongo;

import it.pagopa.pn.externalchannels.entities.discardedmessage.DiscardedMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoDiscardedMessageRepository extends MongoRepository<DiscardedMessage, String> {

}
