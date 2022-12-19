package it.pagopa.pn.externalchannels.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConfigurationProperties(prefix = "pn.external-channels")
@Import({SharedAutoConfiguration.class, PnAuditLogBuilder.class})
@Data
public class PnExternalChannelsProperties {

    private Topics topics;

    private String safeStorageBaseUrl;

    private String safeStorageCxId;

    private String safeStorageCxIdUpdatemetadata;

    private int cacheExpireAfterDays;

    private int cacheMaxSize;

    @Data
    public static class Topics {

        private String toDeliveryPush;

    }


}
