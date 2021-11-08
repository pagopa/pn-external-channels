package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.api.dto.events.ServiceLevelType;
import it.pagopa.pn.externalchannels.config.properties.JobProperties;
import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;
import it.pagopa.pn.externalchannels.entities.csvtemplate.CsvTemplate;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.pojos.CsvTransformationResult;
import it.pagopa.pn.externalchannels.pojos.ElaborationResult;
import it.pagopa.pn.externalchannels.repositories.cassandra.CsvTemplateRepository;
import it.pagopa.pn.externalchannels.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static it.pagopa.pn.externalchannels.service.TestUtils.toJson;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = { CsvServiceTest.SpringTestConfiguration.class, CsvServiceImpl.class})
class CsvServiceTest {

    @TestConfiguration
    @TestPropertySource("classpath:application.yaml")
    static class SpringTestConfiguration {

        @Bean
        JobProperties jobProperties(){
            return new JobProperties();
        }

    }

    @Value("${job.messages-csv-template-id}")
    private String messagesCsvTemplateId;

    @Value("${job.results-csv-template-id}")
    private String resultCsvTemplateId;

    @MockBean
    CsvTemplateRepository csvTemplateRepository;

    @Autowired
    CsvService csvService;

    @Test
    void shouldCreateDigitalCsv(){
        when(csvTemplateRepository.findFirstByIdCsv(messagesCsvTemplateId)).thenReturn(mockCsvTemplate());
        List<QueuedMessage> qms = Arrays.asList(mockQueuedMessage());
        qms.get(0).setServiceLevel(Constants.PEC);
        CsvTransformationResult res = csvService.queuedMessagesToCsv(qms);
        byte[] expectedBytes = "CODICE ATTO;DESTINATARIO;CAP\r\n123;456;00789\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expectedBytes, res.getCsvContent());
        String expectedFilename = "8585_5066_[0-9]{1,5}_[0-9]{8}_PZ1\\.csv";
        System.out.println("FILENAME: " + res.getFileName());
        assertTrue(res.getFileName().matches(expectedFilename));
    }

    @Test
    void shouldCreatePhysicalCsv(){
        when(csvTemplateRepository.findFirstByIdCsv(messagesCsvTemplateId)).thenReturn(mockCsvTemplate());
        List<QueuedMessage> qms = Arrays.asList(mockQueuedMessage());
        qms.get(0).setServiceLevel(ServiceLevelType.REGISTERED_LETTER_890.name());
        CsvTransformationResult res = csvService.queuedMessagesToCsv(qms);
        byte[] expectedBytes = "CODICE ATTO;DESTINATARIO;CAP\r\n123;456;00789\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expectedBytes, res.getCsvContent());
        String expectedFilename = "8585_5065_[0-9]{1,5}_[0-9]{8}_PZ1\\.csv";
        System.out.println("FILENAME: " + res.getFileName());
        assertTrue(res.getFileName().matches(expectedFilename));
    }

    @Test
    void shouldDiscardMessage(){
        when(csvTemplateRepository.findFirstByIdCsv(messagesCsvTemplateId)).thenReturn(mockCsvTemplate());
        List<QueuedMessage> messagesToDiscard = Arrays.asList(mockQueuedMessage());
        messagesToDiscard.get(0).setSenderId("abc");
        CsvTransformationResult res = csvService.queuedMessagesToCsv(messagesToDiscard);
        assertEquals(1, res.getDiscardedMessages().size());
    }

    @Test
    void shouldReadResult(){
        when(csvTemplateRepository.findFirstByIdCsv(resultCsvTemplateId)).thenReturn(mockCsvTemplate());
        byte[] source = "CODICE ATTO;DESTINATARIO;CAP\r\n123;456;00789\r\n".getBytes(StandardCharsets.UTF_8);
        List<ElaborationResult> elaborationResults = csvService.csvToElaborationResults(source);
        assertEquals(1, elaborationResults.size());
        assertEquals("123", elaborationResults.get(0).getIun());
    }

    private CsvTemplate mockCsvTemplate() {
        CsvTemplate csvTemplate = new CsvTemplate();
        csvTemplate.setIdCsv("8");
        csvTemplate.setIdCsv("8");
        csvTemplate.setDescription("mockCsv");
        csvTemplate.setColumns(toJson(mockCsvTemplateColumns()));
        return csvTemplate;
    }

    private List<Column> mockCsvTemplateColumns() {
        return Arrays.asList(
                new Column("CODICE ATTO", false, "text", "iun", null, 20L, ""),
                new Column("DESTINATARIO", false, "text", "id", null, 44L, ""),
                new Column("CAP", false, "paddedInt", "senderId", null, 5L, "")
        );
    }

    private QueuedMessage mockQueuedMessage() {
        QueuedMessage mess = new QueuedMessage();
        mess.setIun("123");
        mess.setId("456");
        mess.setSenderId("789");
        return mess;
    }

}