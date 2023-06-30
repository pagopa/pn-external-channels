package it.pagopa.pn.externalchannels.dao;

import it.pagopa.pn.commons.abstractions.impl.AbstractDynamoKeyValueStore;
import it.pagopa.pn.commons.exceptions.PnIdConflictException;
import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Slf4j
@Component
public class VerificationCodeEntityDaoDynamo extends AbstractDynamoKeyValueStore<VerificationCodeEntity> {
    
    protected VerificationCodeEntityDaoDynamo(DynamoDbEnhancedClient dynamoDbEnhancedClient, PnExternalChannelsProperties cfg) {
        super(dynamoDbEnhancedClient.table( tableName( cfg), TableSchema.fromClass(VerificationCodeEntity.class)));
    }

    private static String tableName(PnExternalChannelsProperties cfg ) {
        return cfg.getVerificationCodeTableName();
    }

    @Override
    public void putIfAbsent(VerificationCodeEntity verificationCodeEntity) throws PnIdConflictException {
        throw new UnsupportedOperationException();
    }
}
