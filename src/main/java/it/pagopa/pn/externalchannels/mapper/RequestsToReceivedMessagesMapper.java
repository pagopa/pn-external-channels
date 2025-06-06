package it.pagopa.pn.externalchannels.mapper;


import it.pagopa.pn.externalchannels.dao.*;
import it.pagopa.pn.externalchannels.mockreceivedmessage.ReceivedMessage;
import it.pagopa.pn.externalchannels.model.DigitalCourtesyMailRequest;
import it.pagopa.pn.externalchannels.model.DigitalCourtesySmsRequest;
import it.pagopa.pn.externalchannels.model.DigitalNotificationRequest;
import it.pagopa.pn.externalchannels.model.PaperEngageRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class RequestsToReceivedMessagesMapper {
    private RequestsToReceivedMessagesMapper(){}

    public static ReceivedMessageEntity map(DigitalNotificationRequest input) {
        String requestId = input.getRequestId();
        ReceivedMessageEntity receivedMessageEntity = new ReceivedMessageEntity(getIunFromRequestId(requestId), getRecIndexFromRequestId(requestId));
        receivedMessageEntity.setPk(requestId);
        receivedMessageEntity.setDigitalNotificationRequest(SmartMapper.mapToClass(input, ReceivedMessageDigitalNotificationRequestEntity.class));
        receivedMessageEntity.getDigitalNotificationRequest().setMessageText(truncate(receivedMessageEntity.getDigitalNotificationRequest().getMessageText()));

        return receivedMessageEntity;
    }


    public static ReceivedMessageEntity map(DigitalCourtesyMailRequest input) {
        String requestId = input.getRequestId();
        ReceivedMessageEntity receivedMessageEntity = new ReceivedMessageEntity(getIunFromRequestId(requestId), getRecIndexFromRequestId(requestId));
        receivedMessageEntity.setPk(requestId);
        receivedMessageEntity.setDigitalCourtesyMailRequest(SmartMapper.mapToClass(input, ReceivedMessageDigitalCourtesyMailRequestEntity.class));
        receivedMessageEntity.getDigitalCourtesyMailRequest().setMessageText(truncate(receivedMessageEntity.getDigitalCourtesyMailRequest().getMessageText()));

        return receivedMessageEntity;
    }

    public static ReceivedMessageEntity map(DigitalCourtesySmsRequest input) {
        String requestId = input.getRequestId();
        ReceivedMessageEntity receivedMessageEntity = new ReceivedMessageEntity(getIunFromRequestId(requestId), getRecIndexFromRequestId(requestId));
        receivedMessageEntity.setPk(requestId);
        receivedMessageEntity.setDigitalCourtesySmsRequest(SmartMapper.mapToClass(input, ReceivedMessageDigitalCourtesySmsRequestEntity.class));
        receivedMessageEntity.getDigitalCourtesySmsRequest().setMessageText(truncate(receivedMessageEntity.getDigitalCourtesySmsRequest().getMessageText()));

        return receivedMessageEntity;
    }

    public static ReceivedMessageEntity map(PaperEngageRequest input) {
        String requestId = input.getRequestId();
        ReceivedMessageEntity receivedMessageEntity = new ReceivedMessageEntity(getIunFromRequestId(requestId), getRecIndexFromRequestId(requestId));
        receivedMessageEntity.setPk(requestId);
        receivedMessageEntity.setPaperEngageRequest(SmartMapper.mapToClass(input, ReceivedMessagePaperEngageRequestEntity.class));

        return receivedMessageEntity;
    }

    public static ReceivedMessage mapReceivedMessageFromEntityToDto(ReceivedMessageEntity receivedMessageEntity) {
        String iun = getIunFromRequestId(receivedMessageEntity.getPk());
        String recIndex = getRecIndexFromRequestId(receivedMessageEntity.getPk());

        ReceivedMessage receivedMessage = new ReceivedMessage();
        receivedMessage.setRequestId(receivedMessageEntity.getPk());
        receivedMessage.setIun(iun);
        receivedMessage.setRecipientIndex(recIndex != null ? Integer.valueOf(recIndex) : null);
        receivedMessage.setCreated(OffsetDateTime.from(receivedMessageEntity.getCreated().atOffset(ZoneOffset.UTC)));


        if (receivedMessageEntity.getDigitalCourtesyMailRequest() != null)
            receivedMessage.setDigitalCourtesyMailRequest(SmartMapper.mapToClass(receivedMessageEntity.getDigitalCourtesyMailRequest(),
                    it.pagopa.pn.externalchannels.mockreceivedmessage.DigitalCourtesyMailRequest.class));
        if (receivedMessageEntity.getDigitalCourtesySmsRequest() != null)
            receivedMessage.setDigitalCourtesySmsRequest(SmartMapper.mapToClass(receivedMessageEntity.getDigitalCourtesySmsRequest(), it.pagopa.pn.externalchannels.mockreceivedmessage.DigitalCourtesySmsRequest.class));
        if (receivedMessageEntity.getDigitalNotificationRequest() != null)
            receivedMessage.setDigitalNotificationRequest(SmartMapper.mapToClass(receivedMessageEntity.getDigitalNotificationRequest(), it.pagopa.pn.externalchannels.mockreceivedmessage.DigitalNotificationRequest.class));
        if (receivedMessageEntity.getPaperEngageRequest() != null)
            receivedMessage.setPaperEngageRequest(SmartMapper.mapToClass(receivedMessageEntity.getPaperEngageRequest(), it.pagopa.pn.externalchannels.mockreceivedmessage.PaperEngageRequest.class));

        return receivedMessage;
    }


    private static String truncate(String str) {
        if (str != null && str.length() > 300000)
            return str.substring(0,300000);

        return str;
    }

    private static String getIunFromRequestId(String requestId){
        if (requestId.toUpperCase().contains("IUN_"))
        {
            int indexofiun_ = requestId.indexOf("IUN_");

            return requestId.substring(indexofiun_+4, requestId.indexOf(".", indexofiun_) );
        }
        return null;
    }

    private static String getRecIndexFromRequestId(String requestId){
        if (requestId.toUpperCase().contains("RECINDEX_"))
        {
            int indexofrecindex_ = requestId.indexOf("RECINDEX_");

            return requestId.substring(indexofrecindex_+9, requestId.indexOf(".", indexofrecindex_) );
        }
        return null;
    }
}
