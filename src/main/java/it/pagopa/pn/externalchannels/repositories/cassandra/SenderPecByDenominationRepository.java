package it.pagopa.pn.externalchannels.repositories.cassandra;

import it.pagopa.pn.externalchannels.entities.senderpa.SenderPecByDenomination;
import org.springframework.data.repository.CrudRepository;

public interface SenderPecByDenominationRepository extends CrudRepository<SenderPecByDenomination, String> {

    SenderPecByDenomination findFirstByDenomination(String denomination);

}
