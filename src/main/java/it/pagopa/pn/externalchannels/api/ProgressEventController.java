package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.dao.NotificationProgressDao;
import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("progress-event")
@RequiredArgsConstructor
public class ProgressEventController {

    private final NotificationProgressDao notificationProgressDao;

    @GetMapping
    public Mono<ResponseEntity<Collection<NotificationProgress>>> findAll() {
        return Mono.just(ResponseEntity.ok(notificationProgressDao.findAll()));
    }

    @GetMapping("{iun}")
    public Mono<ResponseEntity<NotificationProgress>> findByIun(@PathVariable String iun) {
        Optional<NotificationProgress> response = notificationProgressDao.findByIun(iun);
        return response.map(notificationProgress -> Mono.just(ResponseEntity.ok(notificationProgress))).orElseGet(() ->
                Mono.just(ResponseEntity.notFound().build()));

    }
}
