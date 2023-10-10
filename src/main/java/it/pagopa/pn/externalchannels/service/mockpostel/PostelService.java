package it.pagopa.pn.externalchannels.service.mockpostel;

import it.pagopa.pn.externalchannels.dto.postelmock.NormalizeRequestPostelInput;
import it.pagopa.pn.externalchannels.dto.postelmock.NormalizedAddress;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.*;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileDownloadResponse;
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
                List<NormalizedAddress> normalizedAddressList = checkFirstCapToMockUseCase(normalizeRequestPostelList);

                if (CollectionUtils.isEmpty(normalizedAddressList)) {
                    log.info("start perform callback with error");
                    return performCallbackWithError(normalizzazioneRequest.getRequestId());
                } else {
                    log.info("start perform callback");
                    String csvContent = csvService.writeItemsOnCsvToString(normalizedAddressList);
                    String sha256 = mockPostelUtils.computeSha256(csvContent.getBytes(StandardCharsets.UTF_8));
                    return performCallback(normalizzazioneRequest.getRequestId(), csvContent, sha256);
                }
            }else {
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
        FileDownloadResponse file = pnSafeStorageClient.getFile(uri, true);
        assert file.getDownload() != null;
        return pnSafeStorageClient.downloadContent(file.getDownload().getUrl());
    }

    private OperationResultCodeResponse performCallback(String batchId, String content, String sha256) {
        PreLoadRequestData preLoadRequestData = mockPostelUtils.createPreloadRequest(sha256);
        PreLoadResponseData responseData = addressManagerClient.getPresignedURI(POSTEL_CXID, POSTEL_APIKEY, preLoadRequestData);
        PreLoadResponse preLoadResponse = uploadContent(content, responseData.getPreloads());
        NormalizerCallbackRequest normalizerCallbackRequestOK = mockPostelUtils.createNormalizerCallbackRequest(batchId, preLoadResponse.getKey(), sha256);
        OperationResultCodeResponse operationResultCodeResponse = addressManagerClient.performCallback(POSTEL_CXID, POSTEL_APIKEY, normalizerCallbackRequestOK);
        log.info("operationResultCodeResponse for batchId: [{}] --> code: {}, description: {}, error: {}", batchId, operationResultCodeResponse.getResultCode(),
                operationResultCodeResponse.getResultDescription(), operationResultCodeResponse.getErrorList());
        return operationResultCodeResponse;
    }

    public PreLoadResponse uploadContent(String content, List<PreLoadResponse> preloadResponseList) {
        return Flux.fromIterable(preloadResponseList)
                .flatMap(preLoadResponse -> uploadDownloadClient.uploadContent(content, preLoadResponse))
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

    private List<NormalizedAddress> checkFirstCapToMockUseCase(List<NormalizeRequestPostelInput> normalizeRequestPostelInputList) {
        if (!CollectionUtils.isEmpty(normalizeRequestPostelInputList) && StringUtils.hasText(normalizeRequestPostelInputList.get(0).getCap())) {

            return switch (normalizeRequestPostelInputList.get(0).getCap()) {
                case "11111" -> retrieveNormalizedAddress(normalizeRequestPostelInputList, 1, false); //INDIRIZZO POSTALIZZABILE CORRELATION NON COMPLETO
                case "22222" -> retrieveNormalizedAddress(normalizeRequestPostelInputList, 0, true); //INDIRIZZO NON POSTALIZZABILE
                case "33333" -> Collections.emptyList(); //ERRORE IN CALLBACK RESPONSE
                default -> retrieveNormalizedAddress(normalizeRequestPostelInputList, 1, true); //INDIRIZZO POSTALIZZABILE
            };
        } else {
            return retrieveNormalizedAddress(normalizeRequestPostelInputList, 1, true); //INDIRIZZO ESTERO (NO CAP) POSTALIZZABILE
        }
    }

    private List<NormalizedAddress> retrieveNormalizedAddress(List<NormalizeRequestPostelInput> inputList, int postalizzabile, boolean complete) {
        List<NormalizedAddress> normalizedAddressList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(inputList)) {

            Map<String, List<NormalizeRequestPostelInput>> map = inputList.stream().collect(groupingBy(input -> input.getIdCodiceCliente().split("#")[0]));

            if (!complete) {
                map.forEach((s, normalizeRequestPostelInputs) -> {
                    if (normalizeRequestPostelInputs.size() > 1) {
                        normalizeRequestPostelInputs.remove(0);
                    }
                });
            }

            map.forEach((s, normalizeRequestPostelInputs) -> normalizedAddressList.addAll(normalizeRequestPostelInputs.stream()
                    .map(input -> {
                        NormalizedAddress normalizedAddress = new NormalizedAddress();
                        normalizedAddress.setId(input.getIdCodiceCliente());
                        normalizedAddress.setFPostalizzabile(postalizzabile);
                        normalizedAddress.setNRisultatoNorm(1);
                        if (postalizzabile == 0) {
                            normalizedAddress.setNErroreNorm(2);
                        }
                        normalizedAddress.setSViaCompletaSpedizione(input.getIndirizzo());
                        normalizedAddress.setSCivicoAltro(input.getIndirizzo());
                        normalizedAddress.setSCap(input.getCap());
                        normalizedAddress.setSComuneSpedizione(input.getLocalita());
                        normalizedAddress.setSFrazioneSpedizione(input.getLocalitaAggiuntiva());
                        normalizedAddress.setSSiglaProv(input.getProvincia());
                        normalizedAddress.setSStatoSpedizione(input.getStato());
                        return normalizedAddress;
                    })
                    .toList()));
        }
        return normalizedAddressList;
    }


}