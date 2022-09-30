package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.dto.safestorage.*;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileCreationResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.UpdateFileMetadataRequest;
import it.pagopa.pn.externalchannels.middleware.safestorage.PnSafeStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.security.MessageDigest;


@Slf4j
@Service
public class SafeStorageService {
    private final PnSafeStorageClient safeStorageClient;

    public SafeStorageService(PnSafeStorageClient safeStorageClient) {
        this.safeStorageClient = safeStorageClient;
    }

    public FileDownloadResponseInt getFile(String fileKey, Boolean metadataOnly) {
        try {
            FileDownloadResponse fileDownloadResponse = safeStorageClient.getFile(fileKey, metadataOnly);

            return getFileDownloadResponseInt(fileDownloadResponse);
        } catch (Exception e) {
//            throw new PnInternalException("Cannot getfileinfo", ERROR_CODE_DELIVERYPUSH_GETFILEERROR, e);
            throw new RuntimeException("Cannot get file info", e);
        }
    }

    private FileDownloadResponseInt getFileDownloadResponseInt(FileDownloadResponse fileDownloadResponse) {
        FileDownloadResponseInt.FileDownloadResponseIntBuilder responseIntBuilder = FileDownloadResponseInt.builder()
                .contentLength(fileDownloadResponse.getContentLength())
                .checksum(fileDownloadResponse.getChecksum())
                .contentType(fileDownloadResponse.getContentType())
                .key(fileDownloadResponse.getKey());

        if(fileDownloadResponse.getDownload() != null){
            responseIntBuilder.download(
                    FileDownloadInfoInt.builder()
                            .retryAfter(fileDownloadResponse.getDownload().getRetryAfter())
                            .url(fileDownloadResponse.getDownload().getUrl())
                            .build()
            );
        }
        
        return responseIntBuilder.build();
    }

    public FileCreationResponseInt createAndUploadContent(FileCreationWithContentRequest fileCreationRequest) {
        try {
            log.debug("Start call createAndUploadFile - documentType={} filesize={}", fileCreationRequest.getDocumentType(), fileCreationRequest.getContent().length);

            String sha256 = computeSha256(fileCreationRequest.getContent());

            FileCreationResponse fileCreationResponse = safeStorageClient.createFile(fileCreationRequest, sha256);

            safeStorageClient.uploadContent(fileCreationRequest, fileCreationResponse, sha256);

            FileCreationResponseInt fileCreationResponseInt = FileCreationResponseInt.builder()
                    .key(fileCreationResponse.getKey())
                    .build();

            log.info("createAndUploadContent file uploaded successfully key={} sha256={}", fileCreationResponseInt.getKey(), sha256);

            return fileCreationResponseInt;
        } catch (Exception e) {
//            throw new PnInternalException("Cannot createfile", ERROR_CODE_DELIVERYPUSH_UPLOADFILEERROR, e);
            throw new RuntimeException("Cannot create file", e);
        }
    }


    public UpdateFileMetadataResponseInt updateFileMetadata(String fileKey, UpdateFileMetadataRequest updateFileMetadataRequest) {
        try {
            log.debug("Start call updateFileMetadata - fileKey={} updateFileMetadataRequest={}", fileKey, updateFileMetadataRequest);

            var res = safeStorageClient.updateFileMetadata(fileKey, updateFileMetadataRequest);

            UpdateFileMetadataResponseInt updateFileMetadataResponseInt = UpdateFileMetadataResponseInt.builder()
                    .resultCode(res.getResultCode())
                    .errorList(res.getErrorList())
                    .resultDescription(res.getResultDescription())
                    .build();

            log.info("updateFileMetadata file endend key={} updateFileMetadataResponseInt={}", fileKey, updateFileMetadataRequest);

            return updateFileMetadataResponseInt;
        } catch (Exception e) {
//            throw new PnInternalException("Cannot updatemetadata", ERROR_CODE_DELIVERYPUSH_UPDATEMETAFILEERROR, e);
            throw new RuntimeException("Cannot update metadata", e);
        }
    }

    
    private String computeSha256( byte[] content ) {

        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest( content );
            return bytesToBase64( encodedHash );
        } catch (Exception e) {
//            throw new PnInternalException("cannot compute sha256", ERROR_CODE_DELIVERYPUSH_ERRORCOMPUTECHECKSUM, exc );
            throw new RuntimeException("Cannot compute sha256", e);
        }
    }

    private static String bytesToBase64(byte[] hash) {
        return Base64Utils.encodeToString( hash );
    }
}
