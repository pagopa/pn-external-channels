package it.pagopa.pn.externalchannels.dao;

import it.pagopa.pn.commons.abstractions.impl.AbstractDynamoKeyValueStore;
import it.pagopa.pn.commons.db.BaseDAO;
import it.pagopa.pn.commons.exceptions.PnIdConflictException;
import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ReceivedMessageEntityDaoDynamo extends BaseDAO<ReceivedMessageEntity> {

    private Duration ttl;

    protected ReceivedMessageEntityDaoDynamo(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                             DynamoDbAsyncClient dynamoDbAsyncClient, PnExternalChannelsProperties cfg) {
        super(dynamoDbEnhancedAsyncClient, dynamoDbAsyncClient, cfg.getReceivedMessageTableName(), ReceivedMessageEntity.class);
        ttl = cfg.getReceivedMessageTtl();
    }

    public Mono<ReceivedMessageEntity> put(ReceivedMessageEntity entity) {
        entity.setTtl(LocalDateTime.now().plus(ttl).atZone(ZoneId.systemDefault()).toEpochSecond());
        return super.put(entity);
    }

    public Flux<ReceivedMessageEntity> listByIunRecipientIndex(String iun, int recipientIndex) {
        log.info("listByIunRecipientIndex iun={} recipientIndex={}", iun, recipientIndex);
        String pk = iun+ReceivedMessageEntity.SEPARATORE+recipientIndex;

        // la più recente per prima
        return super.getBySecondaryIndex(ReceivedMessageEntity.GSI_INDEX_IUN, pk, null)
                .sort(Comparator.comparing(ReceivedMessageEntity::getCreated).reversed());
    }


    public Mono<ReceivedMessageEntity> getByRequestId(String requestId) {
        log.info("getByRequestId requestId={}", requestId);

        return super.get (requestId, null);
    }
}
