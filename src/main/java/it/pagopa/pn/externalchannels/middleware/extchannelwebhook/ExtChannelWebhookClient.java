package it.pagopa.pn.externalchannels.middleware.extchannelwebhook;


import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.OperationResultCodeResponse;
import it.pagopa.pn.externalchannels.generated.openapi.clients.extchannelwebhook.model.PaperProgressStatusEvent;


public interface ExtChannelWebhookClient {

    OperationResultCodeResponse sendPaperProgressStatusRequest(NotificationProgress notificationProgress, PaperProgressStatusEvent event);
}
