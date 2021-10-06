package it.pagopa.pn.externalchannels.arubapec;

import java.util.Optional;

public interface PecMetadataDao {

    void saveMessageMetadata( String messageId, SimpleMessage dto);

    Optional<SimpleMessage> getMessageMetadata(String originalMessageId);

    void remove(String key);

    boolean isEmpty();
}
