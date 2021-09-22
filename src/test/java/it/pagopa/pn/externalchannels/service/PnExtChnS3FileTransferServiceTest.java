package it.pagopa.pn.externalchannels.service;

import com.amazonaws.services.s3.AmazonS3;
import it.pagopa.pn.externalchannels.config.properties.S3Properties;
import org.junit.jupiter.api.BeforeAll;
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

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"file-transfer-service.implementation=aws"})
@ContextConfiguration(classes = { PnExtChnS3FileTransferServiceTest.SpringTestConfiguration.class, PnExtChnS3FileTransferService.class, S3Properties.class})
class PnExtChnS3FileTransferServiceTest {

    @TestConfiguration
    @TestPropertySource(locations = {"classpath:application-dev.yaml", "classpath:application-test.yaml"})
    static class SpringTestConfiguration {

    }

    @Value("${s3.buckets.external-channels-out}")
    private String outBucket;
    @Value("${s3.buckets.external-channels-in}")
    private String inBucket;

    @Autowired
    PnExtChnFileTransferService s3Service;

    @MockBean
    AmazonS3 s3client;

    @BeforeAll
    static void init() {

    }

    @Test
    void shouldTransferCsv(){
        s3Service.transferCsv("TEST_CONTENT".getBytes(StandardCharsets.UTF_8));

        verify(s3client, times(1)).putObject(eq(outBucket), any(), any(), any());
    }

    @Test
    void shouldRetrieveResult(){
        String key = "getkey";
        s3Service.retrieveElaborationResult(key);

        verify(s3client, times(1)).getObject(inBucket, key);
        verify(s3client, times(1)).copyObject(eq(inBucket), eq(key), eq(inBucket), any());
        verify(s3client, times(1)).deleteObject(inBucket, key);
    }


}