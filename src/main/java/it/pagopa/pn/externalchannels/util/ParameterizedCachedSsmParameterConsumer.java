package it.pagopa.pn.externalchannels.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.abstractions.ParameterConsumer;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static it.pagopa.pn.commons.exceptions.PnExceptionsCodes.ERROR_CODE_PN_GENERIC_ERROR;

@Slf4j
public abstract class ParameterizedCachedSsmParameterConsumer implements ParameterConsumer {

    private final SsmClient ssmClient;
    private final Duration cacheExpiration;

    protected ParameterizedCachedSsmParameterConsumer(SsmClient ssmClient, Duration cacheExpiration) {
        this.ssmClient = ssmClient;
        this.cacheExpiration = cacheExpiration;
    }

    private final ConcurrentHashMap<String, ParameterizedCachedSsmParameterConsumer.ExpiringValue> valueCache = new ConcurrentHashMap<>();

    public <T> Optional<T> getParameterValue(String parameterName, Class<T> clazz ) {
        Object optValue = valueCache.computeIfAbsent( parameterName, key -> new ParameterizedCachedSsmParameterConsumer.ExpiringValue())
                .getValueCheckTimestamp();
        if ( optValue == null ) {
            log.debug("Value for {} not in cache",parameterName);
            optValue = getParameter( parameterName, clazz );
            valueCache.put( parameterName, new ParameterizedCachedSsmParameterConsumer.ExpiringValue(optValue, cacheExpiration));
        }
        return (Optional<T>) optValue;
    }

    public <T> List<Optional<T>> getParameterValue(List<String> parametersName, Class<T> clazz ) {
        List<Optional<T>> optValueList = new ArrayList<>(); //lista di N elementi, N = numero di parameterStore
        for(String parameterName: parametersName){
            Optional<T> optValue = (Optional<T>) valueCache.computeIfAbsent(parameterName, key -> new ExpiringValue()).getValueCheckTimestamp();
            if(optValue == null || optValue.isEmpty()){
                log.debug("Value for {} not in cache", parameterName);
                optValue = getParameter(parameterName, clazz);
                valueCache.put(parameterName, new ParameterizedCachedSsmParameterConsumer.ExpiringValue(optValue, cacheExpiration));
            }
            optValueList.add(optValue);
        }
        return optValueList;
    }

    public String getParameter(String parameterName) {
        GetParameterRequest parameterRequest = GetParameterRequest.builder()
                .name(parameterName)
                .build();
        try {
            GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
            return parameterResponse.parameter().value();
        } catch ( SsmException ex) {
            log.info( "Ssm Client exception for parameterName={}", parameterName, ex );
            return null;
        }
    }

    @NonNull
    private <T> Optional<T> getParameter(String parameterName, Class<T> clazz) {
        Optional<T> result = Optional.empty();
        String json = getParameter( parameterName );
        if (StringUtils.hasText( json )) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                result = Optional.of( objectMapper.readValue( json, clazz ) );
            } catch (JsonProcessingException e) {
                throw new PnInternalException( "Unable to deserialize object", ERROR_CODE_PN_GENERIC_ERROR, e );
            }
        }
        return result;
    }

    @Value
    private static class ExpiringValue {
        Object value;
        Instant timestamp;

        private ExpiringValue(){
            this.value = null;
            this.timestamp = Instant.EPOCH;
        }

        public ExpiringValue(Object value, Instant cacheExpiration) {
            this.value = value;
            this.timestamp = cacheExpiration;
        }

        public ExpiringValue(Object value, Duration cacheExpiration) {
            this (value, Instant.now().plus( cacheExpiration ));
        }

        public Object getValueCheckTimestamp() {
            Object result = null;
            if ( Instant.now().isBefore( timestamp ) ){
                result = value;
            }
            return result;
        }
    }
}
