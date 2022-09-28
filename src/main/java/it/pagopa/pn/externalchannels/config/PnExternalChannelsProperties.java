package it.pagopa.pn.externalchannels.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pn.external-channels")
@Data
public class PnExternalChannelsProperties {

    private Topics topics;

    private String safeStorageBaseUrl;

    private String safeStorageCxId;

    private String safeStorageCxIdUpdatemetadata;

    @Data
    public static class Topics {

        private String toDeliveryPushPec;

        private String toDeliveryPushCourtesy;

        private String toDeliveryPushPaper;

    }


}
