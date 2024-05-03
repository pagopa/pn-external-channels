package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.dao.ReceivedMessageEntityDaoDynamo;
import it.pagopa.pn.externalchannels.dto.ReceivedMessage;
import it.pagopa.pn.externalchannels.mapper.RequestsToReceivedMessagesMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReceivedMessageService {

    public final ReceivedMessageEntityDaoDynamo dao;

    public ReceivedMessageService(ReceivedMessageEntityDaoDynamo dao) {
        this.dao = dao;
    }

    public Mono<ReceivedMessage> findByRequestId(String requestId){
        return dao.getByRequestId(requestId)
                .map(RequestsToReceivedMessagesMapper::mapReceivedMessageFromEntityToDto);
    }

    public Flux<ReceivedMessage> findByIunRecIndex(String iun, int recipientIndex){
        return dao.listByIunRecipientIndex(iun, recipientIndex)
                .map(RequestsToReceivedMessagesMapper::mapReceivedMessageFromEntityToDto);
    }

}
