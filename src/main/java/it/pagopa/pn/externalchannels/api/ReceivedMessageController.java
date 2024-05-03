package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.dto.ReceivedMessage;
import it.pagopa.pn.externalchannels.service.ReceivedMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/external-channels/received-message")
public class ReceivedMessageController {

    private final ReceivedMessageService service;

    @GetMapping("{requestId}")
    public Mono<ResponseEntity<ReceivedMessage>> getReceivedMessage(@PathVariable String requestId){
        return service.findByRequestId(requestId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("{iun}/{recipientIndex}")
    public Mono<ResponseEntity<List<ReceivedMessage>>> getReceivedMessages(@PathVariable String iun, @PathVariable int recipientIndex){
        return service.findByIunRecIndex(iun, recipientIndex)
                .collectSortedList(Comparator.comparing(ReceivedMessage::getCreated).reversed())
                .flatMap(m -> Mono.just(ResponseEntity.ok().body(m)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
