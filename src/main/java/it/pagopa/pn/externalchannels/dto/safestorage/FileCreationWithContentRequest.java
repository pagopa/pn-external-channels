package it.pagopa.pn.externalchannels.dto.safestorage;

import it.pagopa.pn.externalchannels.generated.openapi.clients.safestorage.model.FileCreationRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileCreationWithContentRequest extends FileCreationRequest {

    @ToString.Exclude
    private byte[] content;
}
