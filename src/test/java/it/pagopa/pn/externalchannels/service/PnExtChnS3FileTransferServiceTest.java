package it.pagopa.pn.externalchannels.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"file-transfer-service.implementation=aws"})
@ContextConfiguration(classes = { PnExtChnS3FileTransferServiceTest.SpringTestConfiguration.class, PnExtChnS3FileTransferService.class, S3Properties.class})
class PnExtChnS3FileTransferServiceTest {

    @TestConfiguration
    @TestPropertySource(locations = {"classpath:application.yaml", "classpath:application-test.yaml"})
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
    void shouldGetDownloadLink() throws MalformedURLException {
        when(s3client.generatePresignedUrl(any(), any(), any(), any()))
                .thenReturn(new URL("https://test.com"));
        String link = s3Service.getDownloadLink("key");
        assertNotNull(link);
    }

    @Test
    void shouldTransferCsv(){
        s3Service.transferCsv("TEST_CONTENT".getBytes(StandardCharsets.UTF_8));

        verify(s3client, times(1)).putObject(eq(outBucket), any(), any(), any());
    }

    @Test
    void shouldRetrieveResult() throws IOException {
        String key = "getkey";

        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new S3ObjectInputStream(new ByteArrayInputStream("".getBytes()), null));
        when(s3client.getObject(anyString(), eq(key))).thenReturn(s3Object);

        s3Service.retrieveCsv(key);

        verify(s3client, times(1)).getObject(inBucket, key);
        verify(s3client, times(1)).copyObject(eq(inBucket), eq(key), eq(inBucket), any());
        verify(s3client, times(1)).deleteObject(inBucket, key);
    }


}