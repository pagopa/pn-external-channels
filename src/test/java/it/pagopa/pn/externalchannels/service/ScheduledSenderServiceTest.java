package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.api.dto.events.PnExtChnProgressStatus;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.pojos.CsvTransformationResult;
import it.pagopa.pn.externalchannels.repositories.cassandra.QueuedMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = { ScheduledSenderServiceTest.SpringTestConfiguration.class, ScheduledSenderService.class })
class ScheduledSenderServiceTest {

    @TestConfiguration
    @TestPropertySource("classpath:application.yaml")
    static class SpringTestConfiguration {
        @Bean
        public ModelMapper modelMapper(){
            return new ModelMapper();
        }
    }

    @MockBean
    PnExtChnFileTransferService fileTransferService;

    @MockBean
    CsvService csvService;

    @MockBean
    PnExtChnService pnExtChnService;

    @MockBean
    QueuedMessageRepository queuedMessageRepository;

    @Autowired
    ScheduledSenderService scheduledSenderService;

    @Captor
    ArgumentCaptor<PnExtChnProgressStatus> statusCaptor;

    @BeforeEach
    void init(){

    }

    @Test
    void shouldDiscardMessagesAndSkipTransfer(){
        when(csvService.queuedMessagesToCsv(anyList())).thenReturn(mockErrorCsvResult());
        when(queuedMessageRepository.findByEventStatus(anyString())).thenReturn(Arrays.asList(mockQueuedMessage()));

        scheduledSenderService.retrieveAndSendNotifications();

        verify(fileTransferService, never()).transferCsv(any());
        verify(pnExtChnService).produceStatusMessage(any(), any(), any(), statusCaptor.capture(),
                any(), anyInt(), any(), any());

        List<PnExtChnProgressStatus> statuses = statusCaptor.getAllValues().stream()
                .distinct().collect(Collectors.toList());

        assertTrue(() -> statuses.size() == 1);
        assertEquals(PnExtChnProgressStatus.PERMANENT_FAIL, statuses.get(0));
    }

    @Test
    void shouldProcessMessagesAndTransfer(){
        when(csvService.queuedMessagesToCsv(anyList())).thenReturn(mockCsvResult());
        when(queuedMessageRepository.findByEventStatus(anyString())).thenReturn(Arrays.asList(mockQueuedMessage()));

        scheduledSenderService.retrieveAndSendNotifications();

        verify(fileTransferService, times(1)).transferCsv(any());
    }

    private QueuedMessage mockQueuedMessage() {
        QueuedMessage mess = new QueuedMessage();
        return mess;
    }

    private CsvTransformationResult mockErrorCsvResult(){
        return new CsvTransformationResult(Arrays.asList(mockQueuedMessage()), null);
    }

    private CsvTransformationResult mockCsvResult(){
        return new CsvTransformationResult(Collections.emptyList(), "MOCK_CONTENT".getBytes(StandardCharsets.UTF_8));
    }

}