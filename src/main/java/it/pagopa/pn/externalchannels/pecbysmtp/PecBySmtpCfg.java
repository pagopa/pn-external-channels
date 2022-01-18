package it.pagopa.pn.externalchannels.pecbysmtp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties( prefix = "pec-by-smtp")
@Data
public class PecBySmtpCfg {

    private String imapsHost;
    private String smtpsHost;
    private String smtpsFolder;
    private String user;
    private String password;
}
