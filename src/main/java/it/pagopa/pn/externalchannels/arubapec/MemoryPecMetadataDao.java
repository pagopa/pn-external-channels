package it.pagopa.pn.externalchannels.arubapec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class MemoryPecMetadataDao implements PecMetadataDao {

    private final ConcurrentMap<String, SimpleMessage> store = new ConcurrentHashMap<>();

    @Override
    public void saveMessageMetadata(String messageId, SimpleMessage dto) {
        this.store.put( messageId, dto.toBuilder().key(messageId).build());
    }

    @Override
    public Optional<SimpleMessage> getMessageMetadata(String originalMessageId) {
        log.debug("KEYS: " + store.keySet() );
        String msgId = originalMessageId.replaceAll("[<>]", "");
        return Optional.ofNullable( store.get( msgId) );
    }

    @Override
    public void remove(String key) {
        this.store.remove( key );
    }
}
