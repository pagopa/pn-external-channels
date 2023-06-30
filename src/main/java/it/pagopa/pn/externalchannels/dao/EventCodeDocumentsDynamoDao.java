package it.pagopa.pn.externalchannels.dao;

import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.dto.EventCodeMapKey;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "pn.external-channels.use-dynamodb", havingValue = "true")
@Slf4j
public class EventCodeDocumentsDynamoDao implements EventCodeDocumentsDao {

    public static final String EVENT_CODE_DOCUMENTS_PREFIX = "EVENTCODEDOCUMENTS##";

    //(iun, recipient, eventCode) -> EventCodeMapKey
    //(iun, recipient, eventCode) -> elemento 0: "doc1", "doc2"
    //                            -> elemento 1: "doc1", "doc2"
    private final DynamoDbTable<NotificationProgress> dynamoDbTable;

    public EventCodeDocumentsDynamoDao(DynamoDbEnhancedClient dynamoDbEnhancedClient, PnExternalChannelsProperties config) {
        this.dynamoDbTable = dynamoDbEnhancedClient.table(config.getTableName(), TableSchema.fromBean(NotificationProgress.class));
    }


    @Override
    public boolean insert(EventCodeMapKey eventCodeMapKey, List<String> documents) {
        Key key = buildKey(eventCodeMapKey);
        NotificationProgress item = dynamoDbTable.getItem(key);
        if(item != null) {
            item.getDocuments().add(documents);
        }
        else {
            LinkedList<List<String>> entry = new LinkedList<>();
            entry.add(documents);
            item = new NotificationProgress();
            item.setIun(EVENT_CODE_DOCUMENTS_PREFIX + eventCodeMapKey.getIun() + "##" + eventCodeMapKey.getCode());
            item.setDestinationAddress(eventCodeMapKey.getRecipient());
            item.setDocuments(entry);
        }
        dynamoDbTable.putItem(item);
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
        Key key = buildKey(eventCodeMapKey);
        NotificationProgress item = dynamoDbTable.getItem(key);
        if( item != null && !item.getDocuments().isEmpty()){
            Optional<List<String>> removed = Optional.of(item.getDocuments().remove(0));
            dynamoDbTable.putItem(item);
            return removed;
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
        Key key = buildKey(eventCodeMapKey);
        NotificationProgress item = dynamoDbTable.getItem(key);
        if(item != null && item.getDocuments().isEmpty()){
            dynamoDbTable.deleteItem(key);
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
        Key key = buildKey(eventCodeMapKey);
        dynamoDbTable.deleteItem(key);
    }

    @Override
    public boolean entryExists(EventCodeMapKey eventCodeMapKey) {
        Key key = buildKey(eventCodeMapKey);
        return dynamoDbTable.getItem(key) != null;
    }

    @Override
    public boolean entryExists(String iun, String recipient, String eventCode) {
        return entryExists(EventCodeMapKey.builder()
                .iun(iun)
                .recipient(recipient)
                .code(eventCode).build());
    }

    private Key buildKey(EventCodeMapKey eventCodeMapKey) {
        String pk = EVENT_CODE_DOCUMENTS_PREFIX + eventCodeMapKey.getIun() + "##" + eventCodeMapKey.getCode();
        String sortKey = eventCodeMapKey.getRecipient();

        return Key.builder()
                .partitionValue(pk)
                .sortValue(sortKey)
                .build();
    }
}
