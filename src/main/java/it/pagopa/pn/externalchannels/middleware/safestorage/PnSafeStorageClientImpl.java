package it.pagopa.pn.externalchannels.middleware.safestorage;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.api.FileDownloadApi;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.api.FileMetadataUpdateApi;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.api.FileUploadApi;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileCreationResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.OperationResultCodeResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.UpdateFileMetadataRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@Component
@EnableRetry
public class PnSafeStorageClientImpl implements PnSafeStorageClient {

    private final FileDownloadApi fileDownloadApi;
    private final FileUploadApi fileUploadApi;
    private final FileMetadataUpdateApi fileMetadataUpdateApi;
    private final PnExternalChannelsProperties cfg;
    private final RestTemplate restTemplate;

    public PnSafeStorageClientImpl(@Qualifier("withTracing") RestTemplate restTemplate, PnExternalChannelsProperties cfg) {
        it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.ApiClient newApiClient = new it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.ApiClient(restTemplate);
        newApiClient.setBasePath(cfg.getSafeStorageBaseUrl());

        this.fileDownloadApi = new FileDownloadApi(newApiClient);
        this.fileUploadApi = new FileUploadApi(newApiClient);
        this.fileMetadataUpdateApi = new FileMetadataUpdateApi(newApiClient);
        this.restTemplate = restTemplate;
        this.cfg = cfg;
    }

    @Override
    public FileDownloadResponse getFile(String fileKey, Boolean metadataOnly) {
        log.debug("Start call getFile - fileKey={} metadataOnly={}", fileKey, metadataOnly);
        // elimino eventuale prefisso di safestorage
        fileKey = fileKey.replace(SAFE_STORAGE_URL_PREFIX, "");
        return fileDownloadApi.getFile(fileKey, this.cfg.getSafeStorageCxId(), metadataOnly);
    }

    @Override
    public FileCreationResponse createFile(FileCreationWithContentRequest fileCreationRequest, String sha256) {
        log.debug("Start call createFile - documentType={} filesize={} sha256={}", fileCreationRequest.getDocumentType(), fileCreationRequest.getContent().length, sha256);

        FileCreationResponse fileCreationResponse = fileUploadApi.createFile(this.cfg.getSafeStorageCxId(), "SHA-256", sha256, fileCreationRequest);

        log.debug("End call createFile, created file with key={}", fileCreationResponse.getKey());

        return fileCreationResponse;
    }

    @Override
    @Retryable(
            value = {PnInternalException.class},
            maxAttempts = 3,
            backoff = @Backoff(random = true, delay = 500, maxDelay = 1000, multiplier = 2)
    )
    public OperationResultCodeResponse updateFileMetadata(String fileKey, UpdateFileMetadataRequest request) {
        try {
            log.debug("Start call updateFileMetadata - fileKey={} request={}", fileKey, request);

            OperationResultCodeResponse operationResultCodeResponse = fileMetadataUpdateApi.updateFileMetadata(fileKey, this.cfg.getSafeStorageCxIdUpdatemetadata(), request);

            log.debug("End call updateFileMetadata, updated metadata file with key={}", fileKey);

            return operationResultCodeResponse;
        } catch (RestClientException e) {
            throw new ExternalChannelsMockException("Exception invoking updateFileMetadata", e);
        }
    }


    @Override
    public void uploadContent(FileCreationWithContentRequest fileCreationRequest, FileCreationResponse fileCreationResponse, String sha256) {

        try {

            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add("Content-type", fileCreationRequest.getContentType());
            headers.add("x-amz-checksum-sha256", sha256);
            headers.add("x-amz-meta-secret", fileCreationResponse.getSecret());

            HttpEntity<Resource> req = new HttpEntity<>(new ByteArrayResource(fileCreationRequest.getContent()), headers);

            URI url = URI.create(fileCreationResponse.getUploadUrl());
            HttpMethod method = fileCreationResponse.getUploadMethod() == FileCreationResponse.UploadMethodEnum.POST ? HttpMethod.POST : HttpMethod.PUT;

            ResponseEntity<String> res = restTemplate.exchange(url, method, req, String.class);

            if (res.getStatusCodeValue() != org.springframework.http.HttpStatus.OK.value()) {
                throw new ExternalChannelsMockException("File upload failed");
            }
        } catch (PnInternalException ee) {
            log.error("uploadContent PnInternalException uploading file", ee);
            throw ee;
        } catch (Exception ee) {
            log.error("uploadContent Exception uploading file", ee);
            throw new ExternalChannelsMockException("Exception uploading file", ee);
        }
    }


    @Override
    public byte[] downloadContent(String downloadUrl) {
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    URI.create(downloadUrl),
                    HttpMethod.GET,
                    null,
                    byte[].class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new ExternalChannelsMockException("Content download failed");
            }
        } catch (ExternalChannelsMockException ex) {
            log.error("downloadContent ExternalChannelsMockException downloading content", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("downloadContent Exception downloading content", ex);
            throw new ExternalChannelsMockException("Exception downloading content", ex);
        }
    }

}
