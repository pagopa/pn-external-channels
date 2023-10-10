package it.pagopa.pn.externalchannels.middleware.safestorage;


import it.pagopa.pn.externalchannels.dto.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileCreationResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.OperationResultCodeResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.UpdateFileMetadataRequest;


public interface PnSafeStorageClient {

    String SAFE_STORAGE_URL_PREFIX = "safestorage://";

    FileDownloadResponse getFile(String fileKey, Boolean metadataOnly) ;

    FileCreationResponse createFile(FileCreationWithContentRequest fileCreationRequest, String sha256);

    OperationResultCodeResponse updateFileMetadata(String fileKey, UpdateFileMetadataRequest request);

    void uploadContent(FileCreationWithContentRequest fileCreationRequest, FileCreationResponse fileCreationResponse, String sha256);

    byte[] downloadContent(String downloadUrl);
}
