package it.pagopa.pn.externalchannels.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class S3Properties {

    @Value("${s3.buckets.external-channels-out}")
    private String outBucket;
    @Value("${s3.buckets.external-channels-in}")
    private String inBucket;

    @Value("${s3.retry.delay:0}")
    private Long retryDelay;
    @Value("${s3.retry.attempts:1}")
    private Long retryAttempts;

}
