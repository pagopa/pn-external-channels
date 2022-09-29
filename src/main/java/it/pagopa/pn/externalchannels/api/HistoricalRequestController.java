package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.dto.HistoricalRequest;
import it.pagopa.pn.externalchannels.service.HistoricalRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("historical")
@RequiredArgsConstructor
public class HistoricalRequestController {

    private final HistoricalRequestService historicalRequestService;

    @GetMapping
    public Mono<ResponseEntity<Iterable<HistoricalRequest>>> findAll() {
        Flux<HistoricalRequest> all = historicalRequestService.findAll();
        Mono<List<HistoricalRequest>> listMono = all.collectList();
        return listMono.map(ResponseEntity::ok);

    }

    @GetMapping("{iun}")
    public Mono<ResponseEntity<HistoricalRequest>> findByIun(@PathVariable String iun) {
        Mono<HistoricalRequest> entity = historicalRequestService.findByIun(iun);
        return entity.map(ResponseEntity::ok);
    }
}
