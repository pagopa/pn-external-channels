package it.pagopa.pn.externalchannels.service.mockpostel;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.PreLoadResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class UploadDownloadClient {
    private final WebClient webClient;

    public UploadDownloadClient() {
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector())
                .build();
    }

    public Mono<PreLoadResponse> uploadContent(String content, PreLoadResponse preLoadResponse, String sha256) {
        HttpMethod httpMethod = preLoadResponse.getHttpMethod() == PreLoadResponse.HttpMethodEnum.POST ? HttpMethod.POST : HttpMethod.PUT;
        if(StringUtils.hasText(preLoadResponse.getUrl())) {
            return webClient.method(httpMethod)
                    .uri(URI.create(preLoadResponse.getUrl()))
                    .header("Content-Type", "text/csv")
                    .header("x-amz-meta-secret", preLoadResponse.getSecret())
                    .header("x-amz-checksum-sha256", sha256)
                    .bodyValue(content.getBytes(StandardCharsets.UTF_8))
                    .retrieve()
                    .toBodilessEntity()
                    .thenReturn(preLoadResponse)
                    .onErrorResume(ee -> {
                        log.error("uploadContent Exception uploading file", ee);
                        return Mono.error(new PnInternalException("uploadContent Exception uploading file", "GENERIC-ERROR"));
                    });
        }
        return Mono.error(new PnInternalException("url in preload Response is null", "GENERIC-ERROR"));
    }
}
