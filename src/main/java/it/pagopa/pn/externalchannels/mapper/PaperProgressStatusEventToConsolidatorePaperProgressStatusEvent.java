package it.pagopa.pn.externalchannels.mapper;

import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PaperProgressStatusEvent;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PaperProgressStatusEventAttachments;
import it.pagopa.pn.externalchannels.model.AttachmentDetails;

import java.util.ArrayList;
import java.util.List;

public class PaperProgressStatusEventToConsolidatorePaperProgressStatusEvent {

    private PaperProgressStatusEventToConsolidatorePaperProgressStatusEvent(){}

    public static PaperProgressStatusEvent map(it.pagopa.pn.externalchannels.model.PaperProgressStatusEvent input) {
        PaperProgressStatusEvent output = SmartMapper.mapToClass(input, PaperProgressStatusEvent.class );
        output.setStatusDateTime(input.getStatusDateTime().toInstant());
        output.setClientRequestTimeStamp(input.getClientRequestTimeStamp().toInstant());

        if (input.getAttachments() != null)
        {
            List<PaperProgressStatusEventAttachments> attachments = new ArrayList<>();
            int docId = 0;
            for (AttachmentDetails detail: input.getAttachments()) {
                PaperProgressStatusEventAttachments paperProgressStatusEventAttachments = new PaperProgressStatusEventAttachments();
                paperProgressStatusEventAttachments.setDate(detail.getDate().toInstant());
                paperProgressStatusEventAttachments.setDocumentType(detail.getDocumentType());
                paperProgressStatusEventAttachments.setId(Integer.toString(docId++));
                paperProgressStatusEventAttachments.setUri(detail.getUrl());
                paperProgressStatusEventAttachments.setSha256(detail.getId().split("\\|")[1]);
                attachments.add(paperProgressStatusEventAttachments);
            }
            output.setAttachments(attachments);
        }

        return output;
    }
}
