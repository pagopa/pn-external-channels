package it.pagopa.pn.externalchannels.dao;

import it.pagopa.pn.externalchannels.dto.IunRecipientPair;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe dao che utilizza una ConcurrentHashMap ({@link #database}) per salvare in memoria i codici da inviare.
 * <p>
 * La mappa {@link #iunNumberOfAttempts} viene invece utilizzata per salvare in memoria i numeri di tentativi
 * di richieste diverse (requestId diverse), ma con la stessa coppia iun-destinatario.
 * Questo è il caso avente come address @sequence con gli underscores per dividere i numeri di micro-tentativi da fare.
 * (esempio: mock@sequence.5s-C008_5s-C008_5s-C008_5s-C000.5s-C001.5s-C005.5s-C003).
 * <p>
 * Ogni micro-tentativo corrisponde a un NotificationProgress diverso. In particolare, una volta
 * inviato tutti i codici di un singolo micro-tentativo, il record associato di NotificationProgress verrà cancellato
 * dalla mappa {@link #database} (ma non dalla mappa {@link #iunNumberOfAttempts}).
 */
@Repository
@Slf4j
public class NotificationProgressInMemoryDao implements NotificationProgressDao {

    //(iun, recipient) -> NotificationProgress
    private final ConcurrentHashMap<IunRecipientPair, NotificationProgress> database;

    private final ConcurrentHashMap<IunRecipientPair, Integer> iunNumberOfAttempts;


    public NotificationProgressInMemoryDao() {
        this.database = new ConcurrentHashMap<>();
        this.iunNumberOfAttempts = new ConcurrentHashMap<>();
    }


    @Override
    public boolean insert(NotificationProgress notificationProgress) {
        IunRecipientPair iunRecipientPair = IunRecipientPair.builder()
                .iun(notificationProgress.getIun())
                .recipient(notificationProgress.getDestinationAddress())
                .build();

        NotificationProgress valueInDatabase = database.putIfAbsent(iunRecipientPair, notificationProgress);

        if (valueInDatabase != null) {
            log.warn("[{}] NotificationProgress did not insert because already exists for recipient {}", notificationProgress.getIun(),
                    notificationProgress.getDestinationAddress());
            return false;
        }
        else {
            log.info("NotificationProgress saved: {}", notificationProgress);
            return true;
        }
    }

    @Override
    public Collection<NotificationProgress> findAll() {
        return database.values();
    }

    @Override
    public Optional<NotificationProgress> findByIunAndRecipient(String iun, String recipient) {
        var iunRecipientPair = IunRecipientPair.builder().iun(iun).recipient(recipient).build();
        NotificationProgress notificationProgress = database.get(iunRecipientPair);
        return Optional.ofNullable(notificationProgress);
    }

    @Override
    public void delete(String iun, String recipient) {
        var iunRecipientPair = IunRecipientPair.builder().iun(iun).recipient(recipient).build();
        database.remove(iunRecipientPair);
    }

    @Override
    public boolean iunWithRecipientAlreadyExists(String iun, String recipient) {
        var iunRecipientPair = IunRecipientPair.builder().iun(iun).recipient(recipient).build();
        return iunNumberOfAttempts.containsKey(iunRecipientPair);
    }

    @Override
    public void incrementNumberOfAttempt(String iun, String recipient) {
        var iunRecipientPair = IunRecipientPair.builder().iun(iun).recipient(recipient).build();
        iunNumberOfAttempts.merge(iunRecipientPair, 1, Integer::sum);
    }

    @Override
    public Integer getNumberOfAttemptsByIun(String iun, String recipient) {
        var iunRecipientPair = IunRecipientPair.builder().iun(iun).recipient(recipient).build();
        return iunNumberOfAttempts.get(iunRecipientPair);
    }

    @Override
    public void deleteNumberOfAttemptsByIun(String iun, String recipient) {
        var iunRecipientPair = IunRecipientPair.builder().iun(iun).recipient(recipient).build();
        iunNumberOfAttempts.remove(iunRecipientPair);
        log.info("Deleted NumberOfAttempts of iun: {} of the recipient: {}", iun, recipient);
    }


}
