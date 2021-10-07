package it.pagopa.pn.externalchannels.service;

import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import it.pagopa.pn.api.dto.events.EventType;
import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.entities.senderpa.SenderConfigByDenomination;
import it.pagopa.pn.externalchannels.entities.senderpa.SenderPecByDenomination;
import it.pagopa.pn.externalchannels.repositories.cassandra.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.stream.Collectors;

import static it.pagopa.pn.externalchannels.service.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"dev-options.fake-pn-ext-chn-service=false"})
@ContextConfiguration(classes = {
        PnExtChnServiceTest.SpringTestConfiguration.class,
        PnExtChnServiceImpl.class
})
class PnExtChnServiceTest {

    @TestConfiguration
    @TestPropertySource("classpath:application-test.yaml")
    static class SpringTestConfiguration {
        @Bean
        public ModelMapper modelMapper(){
            return new ModelMapper();
        }
    }

    @MockBean
    QueueMessagingTemplate statusQueueMessagingTemplate;

    @MockBean
    QueuedMessageRepository queuedMessageRepository;

    @MockBean
    DiscardedMessageRepository discardedMessageRepository;

    @MockBean
    ResultDescriptorRepository resultDescriptorRepository;

    @MockBean
    SenderConfigByDenominationRepository senderConfigByDenominationRepository;

    @MockBean
    SenderPecByDenominationRepository senderPecByDenominationRepository;

    @Autowired
    PnExtChnService pnExtChnService;

    @BeforeEach
    void init(){
        when(senderConfigByDenominationRepository.findByDenominationAndChannelAndServiceLevel(any(),any(),any()))
                .thenReturn(new SenderConfigByDenomination());
        when(senderPecByDenominationRepository.findFirstByDenomination(any()))
                .thenReturn(new SenderPecByDenomination());
    }

    @Test
    void shouldSavePaperMessage(){
        pnExtChnService.savePaperMessage(mockPaperMessage());
    }

    @Test
    void shouldSaveDigitalMessage(){
        pnExtChnService.saveDigitalMessage(mockPecMessage());
    }

    @Test
    void shouldDiscardMessage(){
        pnExtChnService.discardMessage("{+/ non conforming message", null);
    }

    @Test
    void shouldProcessElaborationResults (){
        when(queuedMessageRepository.findByIunIn(any())).thenReturn(mockElaborationResults().stream()
                .map(er -> {
                    QueuedMessage qm = new QueuedMessage();
                    qm.setIun(er.getIun());
                    return qm;
                }).collect(Collectors.toList())
        );
        when(resultDescriptorRepository.listAll()).thenReturn(mockResultDescriptors());
        pnExtChnService.processElaborationResults(mockElaborationResults());
    }

    @Test
    void shouldProduceStatusMessage (){
        pnExtChnService.produceStatusMessage("123", "123", EventType.SEND_PEC_RESPONSE, PnExtChnProgressStatus.OK, "",
                1, "", null);
    }

}