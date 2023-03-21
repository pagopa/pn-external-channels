package it.pagopa.pn.externalchannels.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@ConfigurationProperties(prefix = "pn.external-channels")
@Import({SharedAutoConfiguration.class, PnAuditLogBuilder.class})
@Data
public class PnExternalChannelsProperties {

    private Topics topics;

    private String safeStorageBaseUrl;

    private String safeStorageCxId;

    private String safeStorageCxIdUpdatemetadata;

    private String extchannelwebhookApiKey;


    private int cacheExpireAfterDays;

    private int cacheMaxSize;

    @Data
    public static class Topics {

        private String toDeliveryPush;

        private String toPaperChannel;

    }

    @Data
    public static class WebhookApikeys {

        private String serviceId;

        private String apiKey;

    }

    private List<WebhookApikeys>  parsedApiKeys = new ArrayList<>();
    public Optional<WebhookApikeys> findExtchannelwebhookApiKey(String serviceId) throws JsonProcessingException {
        if (parsedApiKeys.isEmpty() && StringUtils.hasText(extchannelwebhookApiKey)) {
            ObjectMapper objectMapper = new ObjectMapper();
            parsedApiKeys = objectMapper.readValue(extchannelwebhookApiKey, new TypeReference<List<WebhookApikeys>>() {
            });
        }

        return parsedApiKeys.stream().filter(x -> x.serviceId.equals(serviceId)).findFirst();
    }
}
