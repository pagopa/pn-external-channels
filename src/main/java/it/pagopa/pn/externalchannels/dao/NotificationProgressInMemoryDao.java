package it.pagopa.pn.externalchannels.dao;

import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class NotificationProgressInMemoryDao implements NotificationProgressDao {

    //requestId -> NotificationProgress
    private final ConcurrentHashMap<String, NotificationProgress> database;

    private final ConcurrentHashMap<String, Integer> iunNumberOfAttempts;


    public NotificationProgressInMemoryDao() {
        this.database = new ConcurrentHashMap<>();
        this.iunNumberOfAttempts = new ConcurrentHashMap<>();
    }

    //incrementa il numero di tentativi
    @Override
    public void insert(NotificationProgress notificationProgress) {
        NotificationProgress valueInDatabase = database.putIfAbsent(notificationProgress.getIun(), notificationProgress);

        if (valueInDatabase != null) {
            log.warn("[{}] NotificationProgress did not insert because already exists!", notificationProgress.getIun());
        }
    }

    @Override
    public Collection<NotificationProgress> findAll() {
        return database.values();
    }

    @Override
    public Optional<NotificationProgress> findByIun(String iun) {
        NotificationProgress notificationProgress = database.get(iun);
        return Optional.ofNullable(notificationProgress);
    }

    @Override
    public void delete(String iun) {
        database.remove(iun);
    }

    @Override
    public boolean iunAlreadyExists(String iun) {
        return iunNumberOfAttempts.containsKey(iun);
    }

    @Override
    public void incrementNumberOfAttempt(String iun) {
        iunNumberOfAttempts.merge(iun, 1, Integer::sum);
    }

    @Override
    public Integer getNumberOfAttemptsByIun(String iun) {
        return iunNumberOfAttempts.get(iun);
    }

    @Override
    public void deleteNumberOfAttemptsByIun(String iun) {
        iunNumberOfAttempts.remove(iun);
        log.info("Deleted NumberOfAttempts of iun: {}", iun);
    }


}
