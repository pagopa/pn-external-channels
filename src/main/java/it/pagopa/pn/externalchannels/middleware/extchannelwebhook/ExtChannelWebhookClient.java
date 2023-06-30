package it.pagopa.pn.externalchannels.middleware.extchannelwebhook;


import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.OperationResultCodeResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PaperProgressStatusEvent;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PreLoadRequest;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PreLoadResponse;


public interface ExtChannelWebhookClient {

    OperationResultCodeResponse sendPaperProgressStatusRequest(NotificationProgress notificationProgress, PaperProgressStatusEvent event);

    PreLoadResponse presignedUploadRequest(NotificationProgress notificationProgress, PreLoadRequest preloadRequest);
}
