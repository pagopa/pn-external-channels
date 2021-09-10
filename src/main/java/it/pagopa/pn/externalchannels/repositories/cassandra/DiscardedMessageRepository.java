package it.pagopa.pn.externalchannels.repositories.cassandra;

import it.pagopa.pn.externalchannels.entities.discardedmessage.DiscardedMessage;
import org.springframework.data.repository.CrudRepository;

public interface DiscardedMessageRepository extends CrudRepository<DiscardedMessage, String> {

}
