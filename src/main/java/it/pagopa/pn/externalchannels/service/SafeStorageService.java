package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.dto.safestorage.*;
import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PreLoadRequest;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PreLoadResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileCreationResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.UpdateFileMetadataRequest;
import it.pagopa.pn.externalchannels.middleware.extchannelwebhook.ExtChannelWebhookClient;
import it.pagopa.pn.externalchannels.middleware.safestorage.PnSafeStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.security.MessageDigest;
import java.util.UUID;


@Slf4j
@Service
public class SafeStorageService {
    private final PnSafeStorageClient safeStorageClient;
    private final ExtChannelWebhookClient extChannelWebhookClient;

    public SafeStorageService(PnSafeStorageClient safeStorageClient, ExtChannelWebhookClient extChannelWebhookClient) {
        this.safeStorageClient = safeStorageClient;
        this.extChannelWebhookClient = extChannelWebhookClient;
    }

    public FileDownloadResponseInt getFile(String fileKey, Boolean metadataOnly) {
        try {
            FileDownloadResponse fileDownloadResponse = safeStorageClient.getFile(fileKey, metadataOnly);

            return getFileDownloadResponseInt(fileDownloadResponse);
        } catch (Exception e) {
            throw new ExternalChannelsMockException("Cannot get file info", e);
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

    public FileCreationResponseInt createAndUploadContent(NotificationProgress notificationProgress, FileCreationWithContentRequest fileCreationRequest) {
        if (notificationProgress == null || notificationProgress.getOutput() != NotificationProgress.PROGRESS_OUTPUT_CHANNEL.WEBHOOK_EXT_CHANNEL)
        {
            // nel caso notificationProgress sia nullo o non abbia a che fare con webhook, faccio il caricamento diretto a safe storage
            return createAndUploadContentSafeStorage(fileCreationRequest);
        }
        else {
            // nel caso in cui la richiesta ha ache fare con webhook, il caricamento deve passare per la preload di ext-channel.
            return createAndUploadContentWithExternalChannel(notificationProgress, fileCreationRequest);
        }
    }

    private FileCreationResponseInt createAndUploadContentSafeStorage(FileCreationWithContentRequest fileCreationRequest) {
        try {
            log.debug("Start call createAndUploadContentSafeStorage - documentType={} filesize={}", fileCreationRequest.getDocumentType(), fileCreationRequest.getContent().length);

            String sha256 = computeSha256(fileCreationRequest.getContent());

            FileCreationResponse fileCreationResponse = safeStorageClient.createFile(fileCreationRequest, sha256);

            FileCreationResponseInt fileCreationResponseInt = uploadContent(fileCreationRequest, sha256, fileCreationResponse);

            log.info("createAndUploadContentSafeStorage file uploaded successfully key={} sha256={}", fileCreationResponseInt.getKey(), sha256);

            return fileCreationResponseInt;
        } catch (Exception e) {
            throw new ExternalChannelsMockException("Cannot create file", e);
        }
    }



    private FileCreationResponseInt createAndUploadContentWithExternalChannel(NotificationProgress notificationProgress, FileCreationWithContentRequest fileCreationRequest) {
        try {
            log.debug("Start call createAndUploadContentWithExternalChannel - documentType={} filesize={}", fileCreationRequest.getDocumentType(), fileCreationRequest.getContent().length);

            String sha256 = computeSha256(fileCreationRequest.getContent());

            PreLoadRequest preLoadRequest = new PreLoadRequest();
            preLoadRequest.setSha256(sha256);
            preLoadRequest.setContentType(fileCreationRequest.getContentType());
            preLoadRequest.setPreloadIdx(UUID.randomUUID().toString());

            PreLoadResponse preLoadResponse = extChannelWebhookClient.presignedUploadRequest(notificationProgress, preLoadRequest);

            // traduco la richiesta nel formato di uploadcontent
            FileCreationResponse fileCreationResponse = new FileCreationResponse();
            fileCreationResponse.setKey(preLoadResponse.getKey());
            fileCreationResponse.setSecret(preLoadResponse.getSecret());
            fileCreationResponse.setUploadUrl(preLoadResponse.getUrl());
            fileCreationResponse.setUploadMethod(FileCreationResponse.UploadMethodEnum.fromValue(preLoadResponse.getHttpMethod().getValue()));

            FileCreationResponseInt fileCreationResponseInt = uploadContent(fileCreationRequest, sha256, fileCreationResponse);

            log.info("createAndUploadContentWithExternalChannel file uploaded successfully key={} sha256={}", fileCreationResponseInt.getKey(), sha256);

            return fileCreationResponseInt;
        } catch (Exception e) {
            throw new ExternalChannelsMockException("Cannot create file", e);
        }
    }

    private FileCreationResponseInt uploadContent(FileCreationWithContentRequest fileCreationRequest, String sha256, FileCreationResponse fileCreationResponse) {
        safeStorageClient.uploadContent(fileCreationRequest, fileCreationResponse, sha256);

        return FileCreationResponseInt.builder()
                .key(fileCreationResponse.getKey())
                .sha256(sha256)
                .build();
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
            throw new ExternalChannelsMockException("Cannot update metadata", e);
        }
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
