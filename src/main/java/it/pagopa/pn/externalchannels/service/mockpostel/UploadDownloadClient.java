package it.pagopa.pn.externalchannels.service.mockpostel;

import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.PreLoadResponse;
import it.pagopa.pn.externalchannels.util.MockPostelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class UploadDownloadClient {
    private final WebClient webClient;
    private final MockPostelUtils mockPostelUtils;

    public UploadDownloadClient(MockPostelUtils mockPostelUtils) {
        this.mockPostelUtils = mockPostelUtils;
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector())
                .build();
    }

    public Mono<PreLoadResponse> uploadContent(String content, PreLoadResponse preLoadResponse) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.TEXT_PLAIN_VALUE));
        headers.add("x-amz-checksum-sha256", mockPostelUtils.computeSha256(content.getBytes(StandardCharsets.UTF_8)));
        headers.add("x-amz-meta-secret", preLoadResponse.getSecret());
        ByteArrayResource resource = new ByteArrayResource(content.getBytes());
        assert preLoadResponse.getUrl() != null;
        return webClient.method(HttpMethod.POST)
                .uri(preLoadResponse.getUrl())
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromResource(resource))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(throwable -> log.error("uploadContent PnInternalException uploading file", throwable))
                .map(s -> preLoadResponse);
    }
}
