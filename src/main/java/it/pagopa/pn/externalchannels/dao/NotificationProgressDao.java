package it.pagopa.pn.externalchannels.dao;

import it.pagopa.pn.externalchannels.dto.NotificationProgress;

import java.util.Collection;
import java.util.Optional;

public interface NotificationProgressDao {

    boolean insert(NotificationProgress notificationProgress);
    Collection<NotificationProgress> findAll();
    Optional<NotificationProgress> findByIunAndRecipient(String iun, String recipient);
    void delete(String iun, String recipient);

    boolean iunWithRecipientAlreadyExists(String iun, String recipient);

    void incrementNumberOfAttempt(String iun, String recipient);

    Integer getNumberOfAttemptsByIun(String iun, String recipient);

    void deleteNumberOfAttemptsByIun(String iun, String recipient);
}
