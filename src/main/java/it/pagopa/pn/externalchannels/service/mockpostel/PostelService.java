package it.pagopa.pn.externalchannels.service.mockpostel;

import it.pagopa.pn.externalchannels.dto.postelmock.NormalizeRequestPostelInput;
import it.pagopa.pn.externalchannels.dto.postelmock.NormalizedAddress;
import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.CallbackRequestData;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.PreLoadRequest;
import it.pagopa.pn.externalchannels.generated.openapi.clients.pnaddressmanager.model.PreLoadRequestData;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileCreationResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.externalchannels.middleware.addressmanager.PnAddressManagerClientImpl;
import it.pagopa.pn.externalchannels.middleware.safestorage.PnSafeStorageClient;
import it.pagopa.pn.externalchannels.mock_postel.RequestActivatePostel;
import it.pagopa.pn.externalchannels.mock_postel.ResponseActivatePostel;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@Service
@lombok.CustomLog
public class PostelService {
    private final PnSafeStorageClient pnSafeStorageClient;
    private final PnAddressManagerClientImpl addressManagerClient;
    private static final String POSTEL_CXID = "POSTEL";
    private static final String POSTEL_APIKEY = "test";
    private final CsvService csvService;

    public PostelService(PnSafeStorageClient pnSafeStorageClient,
                         PnAddressManagerClientImpl addressManagerClient, CsvService csvService) {
        this.pnSafeStorageClient = pnSafeStorageClient;
        this.addressManagerClient = addressManagerClient;
        this.csvService = csvService;
    }

    public Mono<ResponseActivatePostel> checkUploadFile(RequestActivatePostel requestActivatePostel) {
        PreLoadRequestData preLoadRequestData = createPreloadRequest();
        ResponseActivatePostel responseActivatePostel = new ResponseActivatePostel();
        return addressManagerClient.getPresignedURI(POSTEL_CXID, POSTEL_APIKEY, preLoadRequestData)
                .flatMap(uriDownload -> {
                    FileDownloadResponse file = pnSafeStorageClient.getFile(requestActivatePostel.getInputFilekey(), true);
                    assert file.getDownload() != null;
                    byte[] download = pnSafeStorageClient.downloadContent(file.getDownload().getUrl());

                    List<NormalizeRequestPostelInput> normalizeRequestPostelList = csvService.readItemsFromCsv(NormalizeRequestPostelInput.class, download, 0);
                    List<NormalizedAddress> normalizedAddressList = checkFirstCapToMockUseCase(normalizeRequestPostelList);

                    if (normalizedAddressList == null) {
                        responseActivatePostel.setResponse(ResponseActivatePostel.ResponseEnum.KO);
                        return Mono.just(responseActivatePostel);
                    } else if (CollectionUtils.isEmpty(normalizedAddressList)) {
                        return performCallbackWithError(requestActivatePostel.getIdLavorazione());
                    }
                    String csvContent = csvService.writeItemsOnCsvToString(normalizedAddressList);
                    FileCreationResponse fileCreationResponse = createAndUploadFile(csvContent);
                    return performCallback(requestActivatePostel.getIdLavorazione(), fileCreationResponse);
                });
    }

    private PreLoadRequestData createPreloadRequest() {
        PreLoadRequestData preLoadRequestData = new PreLoadRequestData();
        PreLoadRequest preLoadRequest = new PreLoadRequest();
        preLoadRequest.setContentType("text/csv");
        preLoadRequest.setPreloadIdx(UUID.randomUUID().toString());
        preLoadRequest.setSha256(computeSha256("test-sha".getBytes(StandardCharsets.UTF_8)));
        preLoadRequestData.setPreloads(List.of(preLoadRequest));
        return preLoadRequestData;
    }

    private Mono<ResponseActivatePostel> performCallback(String batchId, FileCreationResponse fileCreationResponse) {
        CallbackRequestData callbackRequestData = new CallbackRequestData();
        callbackRequestData.setIdLavorazione(batchId);
        callbackRequestData.setPresignedUrl(fileCreationResponse.getKey());
        callbackRequestData.setErrMsg(null);
        callbackRequestData.setIsError(false);

        return addressManagerClient.performCallback(POSTEL_CXID, POSTEL_APIKEY, callbackRequestData)
                .flatMap(callbackResponseData -> {
                    ResponseActivatePostel response = new ResponseActivatePostel();
                    response.setResponse(ResponseActivatePostel.ResponseEnum.OK);
                    return Mono.just(response);
                });
    }

    private Mono<ResponseActivatePostel> performCallbackWithError(String batchId) {
        CallbackRequestData callbackRequestData = new CallbackRequestData();
        callbackRequestData.setIdLavorazione(batchId);
        callbackRequestData.setErrMsg("ERRORE DURANTE L'ELABORAZIONE DEL FILE DI INPUT");
        callbackRequestData.setIsError(true);

        return addressManagerClient.performCallback(POSTEL_CXID, POSTEL_APIKEY, callbackRequestData)
                .flatMap(callbackResponseData -> {
                    ResponseActivatePostel response = new ResponseActivatePostel();
                    response.setResponse(ResponseActivatePostel.ResponseEnum.OK);
                    return Mono.just(response);
                });
    }

    private FileCreationResponse createAndUploadFile(String csvContent) {
        FileCreationResponse fileCreationResponse = null;
        if (StringUtils.hasText(csvContent)) {
            FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
            fileCreationRequest.setContent(csvContent.getBytes(StandardCharsets.UTF_8));
            String sha256 = computeSha256(fileCreationRequest.getContent());
            fileCreationResponse = pnSafeStorageClient.createFile(fileCreationRequest, sha256);
            pnSafeStorageClient.uploadContent(fileCreationRequest, fileCreationResponse, sha256);
        }
        return fileCreationResponse;
    }

    private List<NormalizedAddress> checkFirstCapToMockUseCase(List<NormalizeRequestPostelInput> normalizeRequestPostelInputList) {
        if (!CollectionUtils.isEmpty(normalizeRequestPostelInputList) && StringUtils.hasText(normalizeRequestPostelInputList.get(0).getCap())) {
            return switch (normalizeRequestPostelInputList.get(0).getCap()) {
                case "00000" ->
                    //ERRORE POSTEL ACTIVATE
                        null;
                case "11111" ->
                    //INDIRIZZO POSTALIZZABILE CORRELATION NON COMPLETO
                        retrieveNormalizedAddress(normalizeRequestPostelInputList, 1, false);
                case "22222" ->
                    //INDIRIZZO NON POSTALIZZABILE
                        retrieveNormalizedAddress(normalizeRequestPostelInputList, 0, true);
                case "33333" ->
                    //ERRORE IN CALLBACK RESPONSE
                        Collections.emptyList();
                default ->
                    //INDIRIZZO POSTALIZZABILE
                        retrieveNormalizedAddress(normalizeRequestPostelInputList, 1, true);
            };
        } else {
            //INDIRIZZO ESTERO (NO CAP) POSTALIZZABILE
            return retrieveNormalizedAddress(normalizeRequestPostelInputList, 1, true);
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

    private String computeSha256(byte[] content) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(content);
            return bytesToBase64(encodedHash);
        } catch (Exception e) {
            throw new ExternalChannelsMockException("Cannot compute sha256", e);
        }
    }

    private static String bytesToBase64(byte[] hash) {
        return Base64Utils.encodeToString(hash);
    }

}