package it.pagopa.pn.externalchannels.dto.safestorage;

import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileCreationRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileCreationWithContentRequest extends FileCreationRequest {
    private byte[] content;
}
