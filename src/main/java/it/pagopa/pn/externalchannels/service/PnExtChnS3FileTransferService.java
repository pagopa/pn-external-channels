package it.pagopa.pn.externalchannels.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
@Slf4j
@ConditionalOnProperty(name = "file-transfer-service.implementation", havingValue = "aws")
public class PnExtChnS3FileTransferService implements PnExtChnFileTransferService {

    private static final String CSV_NAME = "messages_%s.csv";
    private static final String OK_EXTENSION = ".ok";

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
    public void transferCsv(byte[] csv) {
        log.info("PnExtChnS3FileTransferService - transferCsv - START");

        ByteArrayInputStream is = new ByteArrayInputStream(csv);
        String key = String.format(CSV_NAME, System.currentTimeMillis());

        if(!s3client.doesBucketExistV2(s3Properties.getOutBucket()))
            s3client.createBucket(s3Properties.getOutBucket());

        log.info("Write key " + key);
        attempt(() -> s3client.putObject(s3Properties.getOutBucket(), key, is, null));

        log.info("PnExtChnS3FileTransferService - transferCsv - END");
    }

    @Override
    public Map<String, String> retrieveElaborationResult(String key) {
        log.info("PnExtChnS3FileTransferService - retrieveElaborationResult - START");

        if(!s3client.doesBucketExistV2(s3Properties.getInBucket()))
            s3client.createBucket(s3Properties.getInBucket());

        log.info("Read key " + key);
        S3Object result = attempt(() -> s3client.getObject(s3Properties.getInBucket(), key));
        log.info("Duplicate and rename");
        attempt(() -> s3client.copyObject(s3Properties.getInBucket(), key, s3Properties.getInBucket(), key + OK_EXTENSION));
        log.info("Delete old");
        attempt(() -> s3client.deleteObject(s3Properties.getInBucket(), key));

        log.info("PnExtChnS3FileTransferService - retrieveElaborationResult - END");
        return new HashMap<>(); // STUB RESULT
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
