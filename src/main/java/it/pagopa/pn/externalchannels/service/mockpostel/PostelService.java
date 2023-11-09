package it.pagopa.pn.externalchannels.service.mockpostel;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.externalchannels.dto.postelmock.NormalizeRequestPostelInput;
import it.pagopa.pn.externalchannels.dto.postelmock.NormalizedAddress;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.*;
import it.pagopa.pn.externalchannels.middleware.addressmanager.PnAddressManagerClientImpl;
import it.pagopa.pn.externalchannels.middleware.safestorage.PnSafeStorageClient;
import it.pagopa.pn.externalchannels.mock_postel.NormalizzazioneRequest;
import it.pagopa.pn.externalchannels.mock_postel.NormalizzazioneResponse;
import it.pagopa.pn.externalchannels.util.MockPostelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static it.pagopa.pn.externalchannels.middleware.safestorage.PnSafeStorageClient.SAFE_STORAGE_URL_PREFIX;
import static java.util.stream.Collectors.groupingBy;

@Service
@lombok.CustomLog
@Slf4j
public class PostelService {
    private final PnSafeStorageClient pnSafeStorageClient;
    private final PnAddressManagerClientImpl addressManagerClient;
    private final UploadDownloadClient uploadDownloadClient;
    private final MockPostelUtils mockPostelUtils;
    private static final String POSTEL_CXID = "POSTEL";
    private static final String POSTEL_APIKEY = "test";
    private static final String REQUEST_ID_ERROR_PREFIX = "ACTIVATE_ERROR";
    private final CsvService csvService;


    @Qualifier("addressManagerScheduler")
    private final Scheduler scheduler;

    public PostelService(PnSafeStorageClient pnSafeStorageClient,
                         PnAddressManagerClientImpl addressManagerClient,
                         UploadDownloadClient uploadDownloadClient,
                         MockPostelUtils mockPostelUtils, CsvService csvService,
                         Scheduler scheduler) {
        this.pnSafeStorageClient = pnSafeStorageClient;
        this.addressManagerClient = addressManagerClient;
        this.uploadDownloadClient = uploadDownloadClient;
        this.mockPostelUtils = mockPostelUtils;
        this.csvService = csvService;
        this.scheduler = scheduler;
    }

    public Mono<NormalizzazioneResponse> activateNormalizer(NormalizzazioneRequest normalizzazioneRequest) {
        log.info("start Activate Postel Normalizer");
        Mono.fromCallable(() -> {
            if (!normalizzazioneRequest.getRequestId().startsWith(REQUEST_ID_ERROR_PREFIX)) {
                byte[] fileInputContent = retrieveDataFromCsv(normalizzazioneRequest.getUri());
                log.info("retrieved data from inputCsv");
                List<NormalizeRequestPostelInput> normalizeRequestPostelList = csvService.readItemsFromCsv(NormalizeRequestPostelInput.class, fileInputContent, 0);
                List<NormalizedAddress> normalizedAddressList = checkCodiceClienteAndNormalizeAddresses(normalizeRequestPostelList);

                if (CollectionUtils.isEmpty(normalizedAddressList)) {
                    log.info("start perform callback with error");
                    return performCallbackWithError(normalizzazioneRequest.getRequestId());
                } else {
                    log.info("start perform callback");
                    String csvContent = csvService.writeItemsOnCsvToString(normalizedAddressList);
                    String sha256 = mockPostelUtils.computeSha256(csvContent.getBytes(StandardCharsets.UTF_8));
                    return performCallback(normalizzazioneRequest.getRequestId(), csvContent, sha256);
                }
            } else {
                log.info("callback skipped for activate KO");
                return Mono.empty();
            }
        }).subscribeOn(scheduler).subscribe();


        if (normalizzazioneRequest.getRequestId().startsWith(REQUEST_ID_ERROR_PREFIX)) {
            log.info("Response KO for requestId [{}]", normalizzazioneRequest.getRequestId());
            return Mono.just(mockPostelUtils.getNormalizzazioneKO(normalizzazioneRequest.getRequestId()));
        } else {
            log.info("Response OK for requestId [{}]", normalizzazioneRequest.getRequestId());
            return Mono.just(mockPostelUtils.getNormalizzazioneOK(normalizzazioneRequest.getRequestId()));
        }
    }

    private byte[] retrieveDataFromCsv(String uri) {
        String finalFileKey = uri.replace(SAFE_STORAGE_URL_PREFIX, "");
        FileDownloadResponse file = addressManagerClient.getFile(POSTEL_CXID, POSTEL_APIKEY, finalFileKey);
        if (file.getDownload() != null) {
            return pnSafeStorageClient.downloadContent(file.getDownload().getUrl());
        }
        log.debug("file download data is empty");
        throw new PnInternalException("error during retrieve file from address manager", "file data is empty");
    }

    private OperationResultCodeResponse performCallback(String batchId, String content, String sha256) {
        OperationResultCodeResponse operationResultCodeResponse = new OperationResultCodeResponse();
        PreLoadRequestData preLoadRequestData = mockPostelUtils.createPreloadRequest(sha256);
        PreLoadResponseData responseData = addressManagerClient.getPresignedURI(POSTEL_CXID, POSTEL_APIKEY, preLoadRequestData);
        if (responseData != null) {
            PreLoadResponse preLoadResponse = uploadContent(content, responseData.getPreloads(), sha256);
            NormalizerCallbackRequest normalizerCallbackRequestOK = mockPostelUtils.createNormalizerCallbackRequest(batchId, preLoadResponse.getKey(), sha256);
            operationResultCodeResponse = addressManagerClient.performCallback(POSTEL_CXID, POSTEL_APIKEY, normalizerCallbackRequestOK);
            log.info("operationResultCodeResponse for batchId: [{}] --> code: {}, description: {}, error: {}", batchId, operationResultCodeResponse.getResultCode(),
                    operationResultCodeResponse.getResultDescription(), operationResultCodeResponse.getErrorList());
        }
        return operationResultCodeResponse;
    }

    public PreLoadResponse uploadContent(String content, List<PreLoadResponse> preloadResponseList, String sha256) {
        return Flux.fromIterable(preloadResponseList)
                .flatMap(preLoadResponse -> uploadDownloadClient.uploadContent(content, preLoadResponse, sha256))
                .doOnNext(preLoadResponse -> log.info("NORMALIZZAZIONE - uploadContent OK"))
                .blockFirst();
    }

    private OperationResultCodeResponse performCallbackWithError(String batchId) {
        NormalizerCallbackRequest callbackRequest = new NormalizerCallbackRequest();
        callbackRequest.setRequestId(batchId);
        callbackRequest.setError("E001");

        OperationResultCodeResponse operationResultCodeResponse = addressManagerClient.performCallback(POSTEL_CXID, POSTEL_APIKEY, callbackRequest);
        log.info("operationResultCodeResponse for batchId: [{}] --> code: {}, description: {}, error: {}", batchId, operationResultCodeResponse.getResultCode(),
                operationResultCodeResponse.getResultDescription(), operationResultCodeResponse.getErrorList());
        return operationResultCodeResponse;
    }

    private List<NormalizedAddress> checkCodiceClienteAndNormalizeAddresses(List<NormalizeRequestPostelInput> normalizeRequestPostelInputList) {
        if (!CollectionUtils.isEmpty(normalizeRequestPostelInputList)) {
            if (normalizeRequestPostelInputList.stream().anyMatch(input -> input.getIdCodiceCliente().startsWith("CALLBACK_ERROR"))) {
                return Collections.emptyList();
            } else {
                return retrieveNormalizedAddress(normalizeRequestPostelInputList);
            }
        } else {
            return Collections.emptyList();
        }
    }


    private List<NormalizedAddress> retrieveNormalizedAddress(List<NormalizeRequestPostelInput> inputList) {
        List<NormalizedAddress> normalizedAddressList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(inputList)) {

            Map<String, List<NormalizeRequestPostelInput>> map = inputList.stream().collect(groupingBy(input -> input.getIdCodiceCliente().split("#")[0]));

            if (map.containsKey("NOT_COMPLETE")) {
                map.forEach((s, normalizeRequestPostelInputs) -> {
                    if (normalizeRequestPostelInputs.size() > 1) {
                        normalizeRequestPostelInputs.remove(0);
                    }
                });
            }
            normalizeAddresses(map, normalizedAddressList);
        }
        return normalizedAddressList;
    }

    private void normalizeAddresses(Map<String, List<NormalizeRequestPostelInput>> map, List<NormalizedAddress> normalizedAddressList) {
        map.forEach((s, normalizeRequestPostelInputs) -> normalizedAddressList.addAll(normalizeRequestPostelInputs.stream()
                .map(input -> {
                    NormalizedAddress normalizedAddress = new NormalizedAddress();
                    normalizedAddress.setId(input.getIdCodiceCliente());
                    evaluateCap(input, normalizedAddress);
                    addressToUpperCase(input, normalizedAddress);
                    return normalizedAddress;
                })
                .toList()));
    }

    private void addressToUpperCase(NormalizeRequestPostelInput input, NormalizedAddress normalizedAddress) {
        if (StringUtils.hasText(input.getIndirizzo())) {
            normalizedAddress.setSViaCompletaSpedizione(input.getIndirizzo().toUpperCase());
        }
        if (StringUtils.hasText(input.getIndirizzoAggiuntivo())) {
            normalizedAddress.setSCivicoAltro(input.getIndirizzoAggiuntivo().toUpperCase());
        }
        if (StringUtils.hasText(input.getCap())) {
            normalizedAddress.setSCap(input.getCap().toUpperCase());
        }
        if (StringUtils.hasText(input.getLocalita())) {
            normalizedAddress.setSComuneSpedizione(input.getLocalita().toUpperCase());
        }
        if (StringUtils.hasText(input.getLocalitaAggiuntiva())) {
            normalizedAddress.setSFrazioneSpedizione(input.getLocalitaAggiuntiva().toUpperCase());
        }
        if (StringUtils.hasText(input.getProvincia())) {
            normalizedAddress.setSSiglaProv(input.getProvincia().toUpperCase());
        }
        if (StringUtils.hasText(input.getStato())) {
            normalizedAddress.setSStatoSpedizione(input.getStato().toUpperCase());
        }
    }

    private void evaluateCap(NormalizeRequestPostelInput input, NormalizedAddress normalizedAddress) {
        switch (input.getCap()) {
            case "11111":
                normalizedAddress.setFPostalizzabile(0);
                normalizedAddress.setNRisultatoNorm(0);
                normalizedAddress.setNErroreNorm(303);
                break;
            case "22222":
                normalizedAddress.setFPostalizzabile(0);
                normalizedAddress.setNRisultatoNorm(0);
                normalizedAddress.setNErroreNorm(901);
                break;
            case "33333":
                normalizedAddress.setFPostalizzabile(0);
                normalizedAddress.setNRisultatoNorm(0);
                normalizedAddress.setNErroreNorm(999);
                break;
            default:
                normalizedAddress.setFPostalizzabile(1);
                normalizedAddress.setNRisultatoNorm(1);
                break;
        }
    }
}