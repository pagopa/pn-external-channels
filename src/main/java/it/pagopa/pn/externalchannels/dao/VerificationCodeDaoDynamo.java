package it.pagopa.pn.externalchannels.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.OperationContext;

import java.util.Optional;
import java.util.Set;

@Component
@AllArgsConstructor
@Slf4j
public class VerificationCodeDaoDynamo implements VerificationCodeDao{

    private final VerificationCodeEntityDaoDynamo dao;
    
    @Override
    public void addVerificationCode(VerificationCodeEntity verificationCodeEntity) {
        dao.put(verificationCodeEntity);
    }

    @Override
    public Optional<VerificationCodeEntity> getVerificationCode(String pk) {
        Key hashKey = Key.builder().partitionValue(pk).build();
        return dao.get(hashKey);
    }
}

