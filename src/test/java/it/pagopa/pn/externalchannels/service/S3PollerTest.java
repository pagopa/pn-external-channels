package it.pagopa.pn.externalchannels.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import it.pagopa.pn.externalchannels.config.properties.S3Properties;
import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;
import it.pagopa.pn.externalchannels.entities.csvtemplate.CsvTemplate;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.pojos.CsvTransformationResult;
import it.pagopa.pn.externalchannels.pojos.ElaborationResult;
import it.pagopa.pn.externalchannels.repositories.cassandra.CsvTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static it.pagopa.pn.externalchannels.service.TestUtils.toJson;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = { S3PollerTest.SpringTestConfiguration.class, S3ResultPollerService.class })
class S3PollerTest {

    @TestConfiguration
    @TestPropertySource("classpath:application.yaml")
    static class SpringTestConfiguration {

    }

    @Autowired
    S3ResultPollerService pollerService;

    @MockBean
    AmazonS3 s3client;

    @MockBean
    S3Properties s3Properties;

    @MockBean
    PnExtChnElaborationResultInboundService service;

    @MockBean
    ListObjectsV2Result result;

    @Test
    void shouldProcess() throws IOException {
        List<S3ObjectSummary> mockSummary = Arrays.asList(
                mockS3ObjectSummery("test1" + PnExtChnS3FileTransferService.OK_EXTENSION),
                mockS3ObjectSummery("test2" + PnExtChnS3FileTransferService.CSV_EXTENSION),
                mockS3ObjectSummery("test3" + PnExtChnS3FileTransferService.CSV_EXTENSION)
        );

        when(result.getObjectSummaries()).thenReturn(mockSummary);

        when(s3client.listObjectsV2((String) any())).thenReturn(result);

        pollerService.pollResultsBucket();

        verify(service).handleElaborationResult(any());
    }

    @Test
    void shouldNotProcess() throws IOException {
        List<S3ObjectSummary> mockSummary = Arrays.asList(
                mockS3ObjectSummery("test1" + PnExtChnS3FileTransferService.OK_EXTENSION),
                mockS3ObjectSummery("test2" + PnExtChnS3FileTransferService.OK_EXTENSION),
                mockS3ObjectSummery("test3" + PnExtChnS3FileTransferService.OK_EXTENSION)
        );

        when(result.getObjectSummaries()).thenReturn(mockSummary);

        when(s3client.listObjectsV2((String) any())).thenReturn(result);

        pollerService.pollResultsBucket();

        verifyNoInteractions(service);
    }

    private S3ObjectSummary mockS3ObjectSummery(String key) {
        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setKey(key);
        return s3ObjectSummary;
    }
}