package it.pagopa.pn.externalchannels.dto.safestorage;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class UpdateFileMetadataResponseInt {
    private String resultCode;

    private String resultDescription;

    private List<String> errorList = null;
}
