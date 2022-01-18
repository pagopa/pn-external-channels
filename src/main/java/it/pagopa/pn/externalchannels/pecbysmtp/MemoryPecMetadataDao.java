package it.pagopa.pn.externalchannels.pecbysmtp;

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
        log.info("Add messageId={}", messageId);
        log.debug("Add messageId={} dto={}", messageId, dto);
        this.store.put( messageId, dto.toBuilder().key(messageId).build());
    }

    @Override
    public Optional<SimpleMessage> getMessageMetadata(String originalMessageId) {
        log.debug("Retrieve messageId={} ; actualKeys={}", originalMessageId, store.keySet() );
        String msgId = originalMessageId.replaceAll("[<>]", "");
        SimpleMessage message = store.get(msgId);
        log.debug("Retrieved message messageId={} ; message={}", originalMessageId, message );
        return Optional.ofNullable(message);
    }

    @Override
    public void remove(String key) {
        log.info("Remove messageId={}", key);
        this.store.remove( key );
    }

    @Override
    public boolean isEmpty() {
        return this.store.isEmpty();
    }
}
