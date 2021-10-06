package it.pagopa.pn.externalchannels.arubapec;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties( prefix = "aruba")
@Data
public class ArubaCfg {

    private String user;

    private String password;
}
