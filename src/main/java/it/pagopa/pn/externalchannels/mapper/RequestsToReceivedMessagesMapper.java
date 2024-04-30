package it.pagopa.pn.externalchannels.mapper;


import it.pagopa.pn.externalchannels.dao.*;
import it.pagopa.pn.externalchannels.model.DigitalCourtesyMailRequest;
import it.pagopa.pn.externalchannels.model.DigitalCourtesySmsRequest;
import it.pagopa.pn.externalchannels.model.DigitalNotificationRequest;
import it.pagopa.pn.externalchannels.model.PaperEngageRequest;

public class RequestsToReceivedMessagesMapper {
    private RequestsToReceivedMessagesMapper(){}

    public static ReceivedMessageEntity map(DigitalNotificationRequest input) {
        String requestId = input.getRequestId();
        ReceivedMessageEntity receivedMessageEntity = new ReceivedMessageEntity(getIunFromRequestId(requestId), getRecIndexFromRequestId(requestId));
        receivedMessageEntity.setPk(requestId);
        receivedMessageEntity.setDigitalNotificationRequest(SmartMapper.mapToClass(input, ReceivedMessageDigitalNotificationRequestEntity.class));

        return receivedMessageEntity;
    }

    public static ReceivedMessageEntity map(DigitalCourtesyMailRequest input) {
        String requestId = input.getRequestId();
        ReceivedMessageEntity receivedMessageEntity = new ReceivedMessageEntity(getIunFromRequestId(requestId), getRecIndexFromRequestId(requestId));
        receivedMessageEntity.setPk(requestId);
        receivedMessageEntity.setDigitalCourtesyMailRequest(SmartMapper.mapToClass(input, ReceivedMessageDigitalCourtesyMailRequestEntity.class));

        return receivedMessageEntity;
    }

    public static ReceivedMessageEntity map(DigitalCourtesySmsRequest input) {
        String requestId = input.getRequestId();
        ReceivedMessageEntity receivedMessageEntity = new ReceivedMessageEntity(getIunFromRequestId(requestId), getRecIndexFromRequestId(requestId));
        receivedMessageEntity.setPk(requestId);
        receivedMessageEntity.setDigitalCourtesySmsRequest(SmartMapper.mapToClass(input, ReceivedMessageDigitalCourtesySmsRequestEntity.class));

        return receivedMessageEntity;
    }

    public static ReceivedMessageEntity map(PaperEngageRequest input) {
        String requestId = input.getRequestId();
        ReceivedMessageEntity receivedMessageEntity = new ReceivedMessageEntity(getIunFromRequestId(requestId), getRecIndexFromRequestId(requestId));
        receivedMessageEntity.setPk(requestId);
        receivedMessageEntity.setPaperEngageRequest(SmartMapper.mapToClass(input, ReceivedMessagePaperEngageRequestEntity.class));

        return receivedMessageEntity;
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
