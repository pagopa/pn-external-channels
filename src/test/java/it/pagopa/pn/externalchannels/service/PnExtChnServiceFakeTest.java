package it.pagopa.pn.externalchannels.service;

import freemarker.template.Configuration;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import it.pagopa.pn.api.dto.events.PnExtChnPaperEvent;
import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.externalchannels.arubapec.ArubaSenderService;
import it.pagopa.pn.externalchannels.config.properties.S3Properties;
import it.pagopa.pn.externalchannels.entities.senderpa.SenderConfigByDenomination;
import it.pagopa.pn.externalchannels.entities.senderpa.SenderPecByDenomination;
import it.pagopa.pn.externalchannels.repositories.cassandra.*;
import it.pagopa.pn.externalchannels.service.pnextchnservice.PnExtChnService;
import it.pagopa.pn.externalchannels.service.pnextchnservice.PnExtChnServiceFakeImpl;
import it.pagopa.pn.externalchannels.util.MessageUtil;
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

import static it.pagopa.pn.externalchannels.service.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        PnExtChnServiceFakeTest.SpringTestConfiguration.class,
        PnExtChnServiceFakeImpl.class,
        MessageUtil.class,
        S3Properties.class
})
class PnExtChnServiceFakeTest {

    @TestConfiguration
    @TestPropertySource("classpath:application-test.yaml")
    static class SpringTestConfiguration {
        @Bean
        public ModelMapper modelMapper(){
            return new ModelMapper();
        }

        @Bean
        public Configuration configuration(){
            return new Configuration();
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

    @MockBean
    ArubaSenderService arubaSenderService;

    @MockBean
    PnExtChnFileTransferService fileTransferService;

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
    void shouldSaveImmediateResponseOkPaperMessage(){
        PnExtChnPaperEvent evt = mockPaperMessage();
        evt.getPayload().getDestinationAddress().setAddress("ImmediateResponse(OK)");
        pnExtChnService.savePaperMessage(evt);    }

    @Test
    void shouldSaveImmediateResponseFailPaperMessage(){
        PnExtChnPaperEvent evt = mockPaperMessage();
        evt.getPayload().getDestinationAddress().setAddress("ImmediateResponse(FAIL)");
        pnExtChnService.savePaperMessage(evt);    }

    @Test
    void shouldSaveImmediateResponseNewAddressPaperMessage(){
        PnExtChnPaperEvent evt = mockPaperMessage();
        evt.getPayload().getDestinationAddress().setAddress("ImmediateResponse(NEW_ADDR:via test 123)");
        pnExtChnService.savePaperMessage(evt);
    }

    @Test
    void shouldRealSendDigitalMessage(){
        PnExtChnPecEvent event = mockPecMessage("1", "abc@aaa.it.real");
        pnExtChnService.saveDigitalMessage(event);
    }

    @Test
    void shouldFakeSendDigitalMessage(){
        PnExtChnPecEvent event = mockPecMessage();
        pnExtChnService.saveDigitalMessage(event);
    }

    @Test
    void shouldFakeSendWorksDigitalMessage(){
        PnExtChnPecEvent event = mockPecMessage("1", "abc@works");
        pnExtChnService.saveDigitalMessage(event);
    }

    @Test
    void shouldFakeSendFailBothDigitalMessage(){
        PnExtChnPecEvent event = mockPecMessage("1", "abc@fail-both");
        pnExtChnService.saveDigitalMessage(event);
    }

    @Test
    void shouldFakeSendNotExistDigitalMessage(){
        PnExtChnPecEvent event = mockPecMessage("1", "abc@do-not-exists");
        pnExtChnService.saveDigitalMessage(event);
    }

}