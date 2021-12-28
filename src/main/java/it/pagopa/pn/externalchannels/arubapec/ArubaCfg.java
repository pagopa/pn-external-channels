package it.pagopa.pn.externalchannels.arubapec;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties( prefix = "aruba")
@Data
public class ArubaCfg {

    private String imapsHost;
    private String smtpsHost;
    private String user;
    private String password;
}
