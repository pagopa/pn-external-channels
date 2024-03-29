package it.pagopa.pn.externalchannels.service.mockpostel;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
    private static final String REQUEST_ID_ERROR_PREFIX = "ACTIVATE_ERROR";
    private final CsvService csvService;
    private final PnExternalChannelsProperties pnExternalChannelsProperties;
    private final AddressUtils addressUtils;

    @Qualifier("addressManagerScheduler")
    private final Scheduler scheduler;

    public PostelService(PnSafeStorageClient pnSafeStorageClient,
                         PnAddressManagerClientImpl addressManagerClient,
                         UploadDownloadClient uploadDownloadClient,
                         MockPostelUtils mockPostelUtils, CsvService csvService,
                         PnExternalChannelsProperties pnExternalChannelsProperties,
                         Scheduler scheduler,
                         AddressUtils addressUtils) {
        this.pnSafeStorageClient = pnSafeStorageClient;
        this.addressManagerClient = addressManagerClient;
        this.uploadDownloadClient = uploadDownloadClient;
        this.mockPostelUtils = mockPostelUtils;
        this.pnExternalChannelsProperties = pnExternalChannelsProperties;
        this.csvService = csvService;
        this.scheduler = scheduler;
        this.addressUtils = addressUtils;
    }

    public Mono<NormalizzazioneResponse> activateNormalizer(NormalizzazioneRequest normalizzazioneRequest) {
        log.info("Callback delay: {}", pnExternalChannelsProperties.getAddressManagerCallbackAfterMinutes());

        Mono.delay(Duration.ofMinutes(pnExternalChannelsProperties.getAddressManagerCallbackAfterMinutes()))
                .flatMap(aLong -> {
                    if (!normalizzazioneRequest.getRequestId().startsWith(REQUEST_ID_ERROR_PREFIX)) {
                        byte[] fileInputContent = retrieveDataFromCsv(normalizzazioneRequest.getUri());
                        log.info("retrieved data from inputCsv");
                        List<NormalizeRequestPostelInput> normalizeRequestPostelList = csvService.readItemsFromCsv(NormalizeRequestPostelInput.class, fileInputContent, 0);
                        List<NormalizedAddress> normalizedAddressList = checkCodiceClienteAndNormalizeAddresses(normalizeRequestPostelList);

                        if (CollectionUtils.isEmpty(normalizedAddressList)) {
                            log.info("start perform callback with error");
                            return Mono.just(performCallbackWithError(normalizzazioneRequest.getRequestId()));
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
        FileDownloadResponse file = addressManagerClient.getFile(pnExternalChannelsProperties.getAddressManagerCxId(), pnExternalChannelsProperties.getAddressManagerApiKey(), finalFileKey);
        if (file.getDownload() != null) {
            return pnSafeStorageClient.downloadContent(file.getDownload().getUrl());
        }
        log.debug("file download data is empty");
        throw new PnInternalException("error during retrieve file from address manager", "file data is empty");
    }

    private Mono<OperationResultCodeResponse> performCallback(String batchId, String content, String sha256) {
        PreLoadRequestData preLoadRequestData = mockPostelUtils.createPreloadRequest(sha256);
        PreLoadResponseData responseData = addressManagerClient.getPresignedURI(pnExternalChannelsProperties.getAddressManagerCxId(), pnExternalChannelsProperties.getAddressManagerApiKey(), preLoadRequestData);
        if (responseData != null) {
            return uploadContent(content, responseData.getPreloads(), sha256)
                    .map(preLoadResponses -> {
                        NormalizerCallbackRequest normalizerCallbackRequestOK = mockPostelUtils.createNormalizerCallbackRequest(batchId, preLoadResponses.get(0).getKey(), sha256);
                        OperationResultCodeResponse operationResultCodeResponse = addressManagerClient.performCallback(pnExternalChannelsProperties.getAddressManagerCxId(), pnExternalChannelsProperties.getAddressManagerApiKey(), normalizerCallbackRequestOK);
                        log.info("operationResultCodeResponse for batchId: [{}] --> code: {}, description: {}, error: {}", batchId, operationResultCodeResponse.getResultCode(),
                                operationResultCodeResponse.getResultDescription(), operationResultCodeResponse.getErrorList());

                        return operationResultCodeResponse;
                    });

        }
        return Mono.just(new OperationResultCodeResponse());
    }

    public Mono<List<PreLoadResponse>> uploadContent(String content, List<PreLoadResponse> preloadResponseList, String sha256) {
        return Flux.fromIterable(preloadResponseList)
                .flatMap(preLoadResponse -> uploadDownloadClient.uploadContent(content, preLoadResponse, sha256))
                .doOnNext(preLoadResponse -> log.info("NORMALIZZAZIONE - uploadContent OK"))
                .collectList();
    }

    private OperationResultCodeResponse performCallbackWithError(String batchId) {
        NormalizerCallbackRequest callbackRequest = new NormalizerCallbackRequest();
        callbackRequest.setRequestId(batchId);
        callbackRequest.setError("E001");

        OperationResultCodeResponse operationResultCodeResponse = addressManagerClient.performCallback(pnExternalChannelsProperties.getAddressManagerCxId(), pnExternalChannelsProperties.getAddressManagerApiKey(), callbackRequest);
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
                    NormalizeRequestPostelInput normalizeRequestPostelInput = addressUtils.normalizeAddress(input);
                    toNormalizedAddress(normalizeRequestPostelInput, normalizedAddress);
                    return normalizedAddress;
                })
                .toList()));
    }
    private void toNormalizedAddress(NormalizeRequestPostelInput input, NormalizedAddress normalizedAddress) {
        normalizedAddress.setSViaCompletaSpedizione(input.getIndirizzo());
        normalizedAddress.setSCivicoAltro(input.getIndirizzoAggiuntivo());
        normalizedAddress.setSCap(input.getCap());
        normalizedAddress.setSComuneSpedizione(input.getLocalita());
        normalizedAddress.setSFrazioneSpedizione(input.getLocalitaAggiuntiva());
        normalizedAddress.setSSiglaProv(input.getProvincia());
        normalizedAddress.setSStatoSpedizione(input.getStato());
    }

    private void evaluateCap(NormalizeRequestPostelInput input, NormalizedAddress normalizedAddress) {
        boolean isItalian = addressUtils.isItalian(input.getStato());
        if (isItalian) {
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
                    try {
                        addressUtils.verifyCapAndCity(input.getCap(), input.getProvincia(), input.getLocalita());
                        normalizedAddress.setFPostalizzabile(1);
                        normalizedAddress.setNRisultatoNorm(1);
                        normalizedAddress.setNErroreNorm(null);
                    } catch (PnInternalException e) {
                        normalizedAddress.setFPostalizzabile(0);
                        normalizedAddress.setNRisultatoNorm(0);
                        normalizedAddress.setNErroreNorm(19);
                    }
            }
        } else {
            try {
                addressUtils.searchCountry(input.getStato());
                normalizedAddress.setFPostalizzabile(1);
                normalizedAddress.setNRisultatoNorm(1);
                normalizedAddress.setNErroreNorm(null);
            } catch (PnInternalException e) {
                normalizedAddress.setFPostalizzabile(0);
                normalizedAddress.setNRisultatoNorm(0);
                normalizedAddress.setNErroreNorm(601);
            }
        }
    }
}