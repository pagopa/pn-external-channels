package it.pagopa.pn.externalchannels.service;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import it.pagopa.pn.externalchannels.binding.PnExtChnProcessor;
import it.pagopa.pn.externalchannels.repositories.cassandra.DiscardedMessageRepository;
import it.pagopa.pn.externalchannels.repositories.cassandra.QueuedMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static it.pagopa.pn.externalchannels.service.TestUtils.mockPaperMessage;
import static it.pagopa.pn.externalchannels.service.TestUtils.mockPecMessage;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = { PnExtChnServiceTest.SpringTestConfiguration.class, PnExtChnServiceImpl.class })
@Disabled("Just switched to SQS, test not results not valid right now")
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
    AmazonSQSAsync amazonSQSAsync;

    @MockBean
    QueuedMessageRepository queuedMessageRepository;

    @MockBean
    DiscardedMessageRepository discardedMessageRepository;

    @Autowired
    PnExtChnService pnExtChnService;

    @BeforeEach
    void init(){

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


}