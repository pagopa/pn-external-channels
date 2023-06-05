package it.pagopa.pn.externalchannels.dao;

import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.util.Collection;
import java.util.Optional;

import static it.pagopa.pn.externalchannels.dao.EventCodeDocumentsDynamoDao.EVENT_CODE_DOCUMENTS_PREFIX;

/**
 * Classe dao che utilizza una ConcurrentHashMap ({@link #database}) per salvare in memoria i codici da inviare.
 * <p>
 * La mappa {@link #iunNumberOfAttempts} viene invece utilizzata per salvare in memoria i numeri di tentativi
 * di richieste diverse (requestId diverse), ma con la stessa coppia iun-destinatario.
 * Questo è il caso avente come address @sequence con gli underscores per dividere i numeri di micro-tentativi da fare.
 * (esempio: mock@sequence.5s-C008_5s-C008_5s-C008_5s-C000.5s-C001.5s-C005.5s-C003).
 * <p>
 * Ogni micro-tentativo corrisponde a un NotificationProgress diverso. In particolare, una volta
 * inviato tutti i codici di un singolo micro-tentativo, il record associato di NotificationProgress verrà cancellato
 * dalla mappa {@link #database} (ma non dalla mappa {@link #iunNumberOfAttempts}).
 */
@Repository
@ConditionalOnProperty(name = "pn.external-channels.use-dynamodb", havingValue = "true")
@Slf4j
public class NotificationProgressDynamoDao implements NotificationProgressDao {

    public static final String ATTRIBUTE_NOT_EXISTS = "attribute_not_exists";

    private static final String ATTEMPT_PK_PREFIX = "ATTEMPT##";

    private final DynamoDbTable<NotificationProgress> dynamoDbTable;


    public NotificationProgressDynamoDao(DynamoDbEnhancedClient dynamoDbEnhancedClient, PnExternalChannelsProperties config) {
        this.dynamoDbTable = dynamoDbEnhancedClient.table(config.getTableName(), TableSchema.fromBean(NotificationProgress.class));
    }


    @Override
    public boolean insert(NotificationProgress notificationProgress) {

        String expression = String.format(
                "%s(%s) AND %s(%s)",
                ATTRIBUTE_NOT_EXISTS,
                NotificationProgress.COL_IUN,
                ATTRIBUTE_NOT_EXISTS,
                NotificationProgress.COL_DESTINATION_ADDRESS
        );

        Expression conditionExpressionPut = Expression.builder()
                .expression(expression)
                .build();


        PutItemEnhancedRequest<NotificationProgress> request = PutItemEnhancedRequest.builder( NotificationProgress.class )
                .item(notificationProgress )
                .conditionExpression( conditionExpressionPut )
                .build();

        try {
            dynamoDbTable.putItem(request);
            log.info("NotificationProgress saved: {}", notificationProgress);
            return true;
        }
        catch (ConditionalCheckFailedException ex) {
            log.warn("[{}] NotificationProgress did not insert because already exists for recipient {}", notificationProgress.getIun(),
                    notificationProgress.getDestinationAddress());
            return false;
        }

    }

    @Override
    public boolean put(NotificationProgress notificationProgress) {
        dynamoDbTable.putItem(notificationProgress);
        log.info("NotificationProgress update: {}", notificationProgress);
        return true;
    }


    @Override
    public Collection<NotificationProgress> findAll() {
        return dynamoDbTable.scan().items()
                .stream()
                .filter(this::filterEntity)
                .toList();
    }

    @Override
    public Optional<NotificationProgress> findByIunAndRecipient(String iun, String recipient) {
        NotificationProgress item = dynamoDbTable.getItem(Key.builder()
                .partitionValue(iun)
                .sortValue(recipient)
                .build());

        if(item == null) {
            return Optional.empty();
        }

        return Optional.of(item);
    }

    @Override
    public void delete(String iun, String recipient) {
        dynamoDbTable.deleteItem(Key.builder()
                .partitionValue(iun)
                .sortValue(recipient)
                .build());
    }

    @Override
    public boolean iunWithRecipientAlreadyExists(String iun, String recipient) {
        String pk = ATTEMPT_PK_PREFIX + iun;
        NotificationProgress item = dynamoDbTable.getItem(Key.builder()
                .partitionValue(pk)
                .sortValue(recipient)
                .build());
        return item != null;
    }

    @Override
    public void incrementNumberOfAttempt(String iun, String recipient) {
        String pk = ATTEMPT_PK_PREFIX + iun;
        NotificationProgress item = dynamoDbTable.getItem(Key.builder()
                .partitionValue(pk)
                .sortValue(recipient)
                .build());

        if(item == null) {
            item = new NotificationProgress();
            item.setIun(pk);
            item.setDestinationAddress(recipient);
            item.setNAttempt(1);
        }
        else {
            item.setNAttempt(item.getNAttempt() + 1);
        }
        dynamoDbTable.putItem(item);
    }

    @Override
    public Integer getNumberOfAttemptsByIun(String iun, String recipient) {
        String pk = ATTEMPT_PK_PREFIX + iun;
        NotificationProgress item = dynamoDbTable.getItem(Key.builder()
                .partitionValue(pk)
                .sortValue(recipient)
                .build());

        return item.getNAttempt();
    }

    @Override
    public void deleteNumberOfAttemptsByIun(String iun, String recipient) {
        String pk = ATTEMPT_PK_PREFIX + iun;

        dynamoDbTable.deleteItem(Key.builder()
                .partitionValue(pk)
                .sortValue(recipient)
                .build()
        );
        log.info("Deleted NumberOfAttempts of iun: {} of the recipient: {}", iun, recipient);
    }

    private boolean filterEntity(NotificationProgress notificationProgress) {
        return (!notificationProgress.getIun().startsWith(ATTEMPT_PK_PREFIX)) &&
                (!notificationProgress.getIun().startsWith(EVENT_CODE_DOCUMENTS_PREFIX));
    }


}
