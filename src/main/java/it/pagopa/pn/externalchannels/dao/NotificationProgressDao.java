package it.pagopa.pn.externalchannels.dao;

import it.pagopa.pn.externalchannels.dto.NotificationProgress;

import java.util.Collection;
import java.util.Optional;

public interface NotificationProgressDao {

    void insert(NotificationProgress notificationProgress);
    Collection<NotificationProgress> findAll();
    Optional<NotificationProgress> findByIun(String iun);
    void delete(String iun);

    boolean iunAlreadyExists(String iun);

    void incrementNumberOfAttempt(String iun);

    Integer getNumberOfAttemptsByIun(String iun);

    void deleteNumberOfAttemptsByIun(String iun);
}
