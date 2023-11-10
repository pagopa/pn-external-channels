package it.pagopa.pn.externalchannels.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
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

    private String cxIdUserAttributes;

    private String cxIdDeliveryPush;

    private boolean useDynamodb;

    private String tableName;

    private String verificationCodeTableName;

    private Integer cacheExpSsmMinutes;

    private String addressManagerBaseUrl;

    private String addressManagerCxId;

    private String addressManagerApiKey;

    @Data
    public static class Topics {

        private String toDeliveryPush;

        private String toPaperChannel;

        private String toUserAttributes;

        private String toInternal;

    }

    @Data
    public static class WebhookApikeys {

        private String serviceId;

        private String apiKey;

    }

    private List<WebhookApikeys>  parsedApiKeys = new ArrayList<>();
    public Optional<WebhookApikeys> findExtchannelwebhookApiKey(String serviceId) throws JsonProcessingException {
        log.info("extchannelwebhookApiKey={}",  extchannelwebhookApiKey);
        if (parsedApiKeys.isEmpty() && StringUtils.hasText(extchannelwebhookApiKey)) {
            ObjectMapper objectMapper = new ObjectMapper();
            parsedApiKeys = objectMapper.readValue(extchannelwebhookApiKey, new TypeReference<List<WebhookApikeys>>() {
            });
            log.info("parsed {}", parsedApiKeys);
        }

        return parsedApiKeys.stream().filter(x -> x.serviceId.equals(serviceId)).findFirst();
    }
}
