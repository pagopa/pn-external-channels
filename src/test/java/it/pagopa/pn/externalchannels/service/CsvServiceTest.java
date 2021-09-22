package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;
import it.pagopa.pn.externalchannels.entities.csvtemplate.CsvTemplate;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.pojos.CsvTransformationResult;
import it.pagopa.pn.externalchannels.repositories.cassandra.CsvTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static it.pagopa.pn.externalchannels.service.TestUtils.toJson;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = { CsvServiceTest.SpringTestConfiguration.class, CsvServiceImpl.class })
class CsvServiceTest {

    @TestConfiguration
    @TestPropertySource("classpath:application-dev.yaml")
    static class SpringTestConfiguration {

    }

    @MockBean
    CsvTemplateRepository csvTemplateRepository;

    @Autowired
    CsvService csvService;

    @Test
    void shouldCreateCsv(){
        when(csvTemplateRepository.findFirstByIdCsv("8")).thenReturn(mockCsvTemplate());
        CsvTransformationResult res = csvService.queuedMessagesToCsv(Arrays.asList(mockQueuedMessage()));
        byte[] expectedBytes = "CODICE ATTO;DESTINATARIO;CAP\r\n123;456;00789\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expectedBytes, res.getCsvContent());
    }

    @Test
    void shouldDiscardMessage(){
        when(csvTemplateRepository.findFirstByIdCsv("8")).thenReturn(mockCsvTemplate());
        List<QueuedMessage> messagesToDiscard = Arrays.asList(mockQueuedMessage());
        messagesToDiscard.get(0).setSenderId("abc");
        CsvTransformationResult res = csvService.queuedMessagesToCsv(messagesToDiscard);
        assertEquals(1, res.getDiscardedMessages().size());
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
                new Column("CODICE ATTO", false, "text", "iun", 20L, ""),
                new Column("DESTINATARIO", false, "text", "id", 44L, ""),
                new Column("CAP", false, "paddedInt", "senderId", 5L, "")
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