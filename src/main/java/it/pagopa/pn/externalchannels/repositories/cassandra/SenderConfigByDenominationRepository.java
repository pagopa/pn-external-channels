package it.pagopa.pn.externalchannels.repositories.cassandra;

import it.pagopa.pn.externalchannels.entities.senderpa.SenderConfigByDenomination;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.repository.CrudRepository;

public interface SenderConfigByDenominationRepository extends CrudRepository<SenderConfigByDenomination, String> {

    @AllowFiltering
    SenderConfigByDenomination findByDenominationAndChannelAndServiceLevel(String denomination, String channel, String serviceLevel);

}
