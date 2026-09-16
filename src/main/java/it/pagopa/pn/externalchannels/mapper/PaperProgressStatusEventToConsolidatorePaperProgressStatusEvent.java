package it.pagopa.pn.externalchannels.mapper;

import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PaperProgressStatusEvent;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PaperProgressStatusEventAttachmentsInner;
import it.pagopa.pn.externalchannels.model.AttachmentDetails;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PaperProgressStatusEventToConsolidatorePaperProgressStatusEvent {

    private PaperProgressStatusEventToConsolidatorePaperProgressStatusEvent(){}

    public static PaperProgressStatusEvent map(it.pagopa.pn.externalchannels.model.PaperProgressStatusEvent input) {
        PaperProgressStatusEvent output = SmartMapper.mapToClass(input, PaperProgressStatusEvent.class );
        output.setStatusDateTime(input.getStatusDateTime().toInstant());
        output.setClientRequestTimeStamp(input.getClientRequestTimeStamp().toInstant());

        if (input.getAttachments() != null)
        {
            List<PaperProgressStatusEventAttachmentsInner> attachments = new ArrayList<>();
            int docId = 0;
            for (AttachmentDetails detail: input.getAttachments()) {
                PaperProgressStatusEventAttachmentsInner paperProgressStatusEventAttachments = new PaperProgressStatusEventAttachmentsInner();
                String attachmentId = Integer.toString(docId++);
                paperProgressStatusEventAttachments.setDate(detail.getDate().toInstant());
                paperProgressStatusEventAttachments.setDocumentType(detail.getDocumentType());
                paperProgressStatusEventAttachments.setId(attachmentId);
                paperProgressStatusEventAttachments.setUri(detail.getUri());
                paperProgressStatusEventAttachments.setSha256(detail.getSha256());

                log.info("for attachmentID= {} detailSource={} detailOrigin={} assignedSourceType={} assignedOriginType={}", attachmentId, detail.getSourceType(), detail.getOriginType(), PaperProgressStatusEventAttachmentsInner.SourceTypeEnum.fromValue(detail.getSourceType().getValue()), PaperProgressStatusEventAttachmentsInner.OriginTypeEnum.fromValue(detail.getOriginType().getValue()));

                paperProgressStatusEventAttachments.setSourceType(detail.getSourceType() == null ? null : PaperProgressStatusEventAttachmentsInner.SourceTypeEnum.fromValue(detail.getSourceType().getValue()));
                paperProgressStatusEventAttachments.setOriginType(detail.getOriginType() == null ? null : PaperProgressStatusEventAttachmentsInner.OriginTypeEnum.fromValue(detail.getOriginType().getValue()));
                attachments.add(paperProgressStatusEventAttachments);
            }
            output.setAttachments(attachments);
        }

        return output;
    }
}
