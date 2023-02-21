package it.pagopa.pn.externalchannels.dao;

import it.pagopa.pn.externalchannels.dto.EventCodeMapKey;
import java.util.List;
import java.util.Optional;

public interface EventCodeDocumentsDao {

    boolean insert(EventCodeMapKey eventCodeMapKey, List<String> documents);
    boolean insert(String iun, String recipient, String eventCode, List<String> documents);

    Optional<List<String>> findByKey(EventCodeMapKey eventCodeMapKey);
    Optional<List<String>> findByIunRecipientAndEventCode(String iun, String recipient, String eventCode);

    void delete(String iun, String recipient, String eventCode);
    void delete(EventCodeMapKey eventCodeMapKey);

    boolean entryExists(EventCodeMapKey eventCodeMapKey);
    boolean entryExists(String iun, String recipient, String eventCode);

}
