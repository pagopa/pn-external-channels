package it.pagopa.pn.externalchannels.dao;

import it.pagopa.pn.externalchannels.dto.EventCodeMapKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class EventCodeDocumentsInMemoryDao implements EventCodeDocumentsDao{

    //(iun, recipient, eventCode) -> EventCodeMapKey
    private final ConcurrentHashMap<EventCodeMapKey, List<List<String>>> database;

    public EventCodeDocumentsInMemoryDao() {
        this.database = new ConcurrentHashMap<>();
    }


    @Override
    public boolean insert(EventCodeMapKey eventCodeMapKey, List<String> documents) {
        if(database.contains(eventCodeMapKey)){
            database.get(eventCodeMapKey).add(documents);
        } else  {
            LinkedList<List<String>> entry = new LinkedList<>();
            entry.add(documents);
            database.put(eventCodeMapKey, entry);
        }

        log.info("Event code document saved: {}", eventCodeMapKey);
        return true;
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
    public Optional<List<String>> consumeByKey(EventCodeMapKey eventCodeMapKey) {
        if(database.get(eventCodeMapKey) != null && !database.get(eventCodeMapKey).isEmpty()){
            return Optional.of(database.get(eventCodeMapKey).remove(0));
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<String>> consumeByIunRecipientAndEventCode(String iun, String recipient, String eventCode) {
        return consumeByKey(
                EventCodeMapKey.builder()
                        .iun(iun)
                        .recipient(recipient)
                        .code(eventCode).build());
    }

    @Override
    public boolean deleteIfEmpty(String iun, String recipient, String eventCode) {
        return deleteIfEmpty(EventCodeMapKey.builder()
                .iun(iun)
                .recipient(recipient)
                .code(eventCode).build());
    }

    @Override
    public boolean deleteIfEmpty(EventCodeMapKey eventCodeMapKey) {
        if(database.contains(eventCodeMapKey) && database.get(eventCodeMapKey).isEmpty()){
            database.remove(eventCodeMapKey);
            return true;
        }
        return false;
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
