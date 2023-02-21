package it.pagopa.pn.externalchannels.dao;

import it.pagopa.pn.externalchannels.dto.EventCodeMapKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class EventCodeDocumentsInMemoryDao implements EventCodeDocumentsDao{

    //(iun, recipient, eventCode) -> EventCodeMapKey
    private final ConcurrentHashMap<EventCodeMapKey, List<String>> database;

    public EventCodeDocumentsInMemoryDao() {
        this.database = new ConcurrentHashMap<>();
    }


    @Override
    public boolean insert(EventCodeMapKey eventCodeMapKey, List<String> documents) {
        List<String> documentsInDatabase = database.putIfAbsent(eventCodeMapKey, documents);

        if (documentsInDatabase != null) {
            log.warn("Document for EventCode {} in map did not insert because already exists for iun {} and recipient {}", eventCodeMapKey.getCode(),
                    eventCodeMapKey.getIun(), eventCodeMapKey.getRecipient());
            return false;
        }
        else {
            log.info("Event code document saved: {}", eventCodeMapKey);
            return true;
        }
    }

    @Override
    public boolean insert(String iun, String recipient, String eventCode, List<String> documents) {
        return insert(
                EventCodeMapKey.builder()
                        .iun(iun)
                        .recipient(recipient)
                        .code(eventCode).build(),
                documents);
    }

    @Override
    public Optional<List<String>> findByKey(EventCodeMapKey eventCodeMapKey) {
        List<String> documents = database.get(eventCodeMapKey);
        return Optional.ofNullable(documents);
    }

    @Override
    public Optional<List<String>> findByIunRecipientAndEventCode(String iun, String recipient, String eventCode) {
        return findByKey(
                EventCodeMapKey.builder()
                        .iun(iun)
                        .recipient(recipient)
                        .code(eventCode).build());
    }

    @Override
    public void delete(String iun, String recipient, String eventCode) {
        delete(EventCodeMapKey.builder()
                .iun(iun)
                .recipient(recipient)
                .code(eventCode).build());
    }

    @Override
    public void delete(EventCodeMapKey eventCodeMapKey) {
        database.remove(eventCodeMapKey);
    }

    @Override
    public boolean entryExists(EventCodeMapKey eventCodeMapKey) {
        return database.containsKey(eventCodeMapKey);
    }

    @Override
    public boolean entryExists(String iun, String recipient, String eventCode) {
        return entryExists(EventCodeMapKey.builder()
                .iun(iun)
                .recipient(recipient)
                .code(eventCode).build());
    }
}
