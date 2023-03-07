package it.pagopa.pn.externalchannels.api.consolidatore;

import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.mapper.ConsolidatoreEngageRequestToEngageRequestMapper;
import it.pagopa.pn.externalchannels.model.consolidatore.OperationResultCodeResponse;
import it.pagopa.pn.externalchannels.model.consolidatore.PaperEngageRequest;
import it.pagopa.pn.externalchannels.service.ExternalChannelsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ConsolidatoreController implements PiattaformaNotificheIngressApi {

    private final ExternalChannelsService externalChannelsService;

    @Override
    public Mono<ResponseEntity<OperationResultCodeResponse>> sendPaperEngageRequest(
            String xPagopaExtchServiceId,
            String xApiKey,
            Mono<PaperEngageRequest> paperEngageRequest,
            ServerWebExchange exchange
    ) {

        return paperEngageRequest
                .doOnNext(request -> log.info("Received CONSOLIDATORE request with requestBody: {}, headers: {}", request, exchange.getRequest().getHeaders()))
                .doOnNext(request -> externalChannelsService.sendPaperEngageRequest(ConsolidatoreEngageRequestToEngageRequestMapper.map(request), NotificationProgress.PROGRESS_OUTPUT_CHANNEL.WEBHOOK_EXT_CHANNEL))
                .map(notificationRequest -> Mono.just(ResponseEntity.noContent().build()))
                .log(this.getClass().getName())
                .onErrorResume(Mono::error).then(Mono.just(ResponseEntity.ok(returnOk())));
    }

    private OperationResultCodeResponse returnOk(){
        OperationResultCodeResponse operationResultCodeResponse = new OperationResultCodeResponse();
        operationResultCodeResponse.setResultCode("200.00");
        operationResultCodeResponse.setClientResponseTimeStamp(OffsetDateTime.now());
        operationResultCodeResponse.setResultDescription("OK");
        operationResultCodeResponse.setErrorList(List.of());
        return operationResultCodeResponse;
    }
}
