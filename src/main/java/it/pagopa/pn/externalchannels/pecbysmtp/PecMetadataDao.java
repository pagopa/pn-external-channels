package it.pagopa.pn.externalchannels.pecbysmtp;

import java.util.Optional;

public interface PecMetadataDao {

    void saveMessageMetadata( String messageId, SimpleMessage dto);

    Optional<SimpleMessage> getMessageMetadata(String messageId);

    void remove(String messageId);

    boolean isEmpty();
}
