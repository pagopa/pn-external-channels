package it.pagopa.pn.externalchannels.pojos;


import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CsvTransformationResult {

    private List<QueuedMessage> discardedMessages = new ArrayList<>();

    private byte[] csvContent;

    private String fileName;

}
