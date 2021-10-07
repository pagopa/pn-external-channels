package it.pagopa.pn.externalchannels.repositories.cassandra;

import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QueuedMessageRepository extends CrudRepository<QueuedMessage, String> {

    @AllowFiltering
    List<QueuedMessage> findByEventStatus(String status);

    @AllowFiltering
    QueuedMessage findByIun(String iun);

    List<QueuedMessage> findByIunIn(List<String> iuns);

}
