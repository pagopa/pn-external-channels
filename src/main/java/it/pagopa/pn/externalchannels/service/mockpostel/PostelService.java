package it.pagopa.pn.externalchannels.service.mockpostel;

import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileCreationResponse;
import it.pagopa.pn.externalchannels.middleware.addressmanager.AddressManagerClient;
import it.pagopa.pn.externalchannels.middleware.safestorage.PnSafeStorageClient;
import it.pagopa.pn.externalchannels.mock_postel.RequestActivatePostel;
import it.pagopa.pn.externalchannels.mock_postel.ResponseActivatePostel;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@lombok.CustomLog
public class PostelService {

    private final PnSafeStorageClient pnSafeStorageClient;
    private final AddressManagerClient addressManagerClient;

    public PostelService(PnSafeStorageClient pnSafeStorageClient,
                         AddressManagerClient addressManagerClient) {
        this.pnSafeStorageClient = pnSafeStorageClient;
        this.addressManagerClient = addressManagerClient;
    }

    public Mono<ResponseActivatePostel> checkUploadFile(RequestActivatePostel requestActivatePostel){
        String fileKey = requestActivatePostel.getFileKey();

        return addressManagerClient.getPresignedURI(fileKey)
                .flatMap(uriDownload -> {
                    byte[] download = pnSafeStorageClient.downloadContent(uriDownload);

                    String csvContent = new String(download, StandardCharsets.UTF_8);
                    String csvContentUpperCase = csvContent.toUpperCase();

                    FileCreationWithContentRequest fileCreationRequest = new FileCreationWithContentRequest();
                    fileCreationRequest.setContent(csvContentUpperCase.getBytes(StandardCharsets.UTF_8));

                    String sha256 = computeSha256(fileCreationRequest.getContent());
                    FileCreationResponse fileCreationResponse = pnSafeStorageClient.createFile(fileCreationRequest, sha256);

                    pnSafeStorageClient.uploadContent(fileCreationRequest, fileCreationResponse, sha256);

                    return addressManagerClient.performCallback(fileCreationResponse.getKey())
                            .flatMap(keyOutput -> {
                                ResponseActivatePostel response = new ResponseActivatePostel();
                                response.setKeyInput(fileCreationResponse.getKey());
                                response.setKeyOutput(keyOutput);
                                return Mono.just(response);
                            });
                });
    }

    private String computeSha256( byte[] content ) {

        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest( content );
            return bytesToBase64( encodedHash );
        } catch (Exception e) {
            throw new ExternalChannelsMockException("Cannot compute sha256", e);
        }
    }

    private static String bytesToBase64(byte[] hash) {
        return Base64Utils.encodeToString( hash );
    }

}