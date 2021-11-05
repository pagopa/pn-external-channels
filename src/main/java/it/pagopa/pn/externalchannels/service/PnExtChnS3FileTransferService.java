package it.pagopa.pn.externalchannels.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import it.pagopa.pn.externalchannels.config.properties.S3Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Supplier;

@Service
@Slf4j
@ConditionalOnProperty(name = "file-transfer-service.implementation", havingValue = "aws")
public class PnExtChnS3FileTransferService implements PnExtChnFileTransferService {

    public static final String CSV_EXTENSION = ".csv";
    public static final String OK_EXTENSION = ".ok";

    @Autowired
    AmazonS3 s3client;

    @Autowired
    S3Properties s3Properties;

    private RetryTemplate retryTemplate;

    @PostConstruct
    private void init(){
        retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(s3Properties.getRetryDelay());
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(s3Properties.getRetryAttempts().intValue());
        retryTemplate.setRetryPolicy(retryPolicy);
    }

    @Override
    public void transferCsv(byte[] csv, String name) {
        log.info("PnExtChnS3FileTransferService - transferCsv - START");

        ByteArrayInputStream is = new ByteArrayInputStream(csv);

        if(!s3client.doesBucketExistV2(s3Properties.getOutBucket()))
            s3client.createBucket(s3Properties.getOutBucket());

        log.info("Write key " + name);
        attempt(() -> s3client.putObject(s3Properties.getOutBucket(), name, is, null));

        log.info("PnExtChnS3FileTransferService - transferCsv - END");
    }

    @Override
    public byte[] retrieveCsv(String key) throws IOException {
        log.info("PnExtChnS3FileTransferService - retrieveElaborationResult - START");

        if(!s3client.doesBucketExistV2(s3Properties.getInBucket()))
            s3client.createBucket(s3Properties.getInBucket());

        log.info("Read key " + key);
        S3Object result = attempt(() -> s3client.getObject(s3Properties.getInBucket(), key));
        log.info("Duplicate and rename");
        attempt(() -> s3client.copyObject(s3Properties.getInBucket(), key, s3Properties.getInBucket(), key + OK_EXTENSION));
        log.info("Delete old");
        attempt(() -> s3client.deleteObject(s3Properties.getInBucket(), key));

        byte[] bytes = IOUtils.toByteArray(result.getObjectContent());
        result.getObjectContent().close();
        log.info("PnExtChnS3FileTransferService - retrieveElaborationResult - END");
        return bytes;
    }

    @Override
    public String getDownloadLink(String attachmentKey) {
        Instant expiry = Instant.now().plus(3, ChronoUnit.HOURS);
        URL url = s3client.generatePresignedUrl(s3Properties.getInBucket(), attachmentKey,
                Date.from(expiry), HttpMethod.GET);
        return url.toString();
    }

    private <T> T attempt(Supplier<T> s){
        return retryTemplate.execute(state -> {
            log.info("Attempt: " + state.getRetryCount());
            return s.get();
        }, ko -> {
            log.error("Retryable operation failed ", ko.getLastThrowable());
            return null;
        });
    }

    private void attempt(Runnable r){
       attempt(() -> {r.run(); return null;});
    }

}
