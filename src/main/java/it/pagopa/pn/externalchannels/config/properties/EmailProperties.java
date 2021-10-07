package it.pagopa.pn.externalchannels.config.properties;

import it.pagopa.pn.externalchannels.service.MessageBodyType;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class EmailProperties {

    @Value("${emailSender.content-type}")
    private MessageBodyType contentType;

    @Value("${emailSender.protocol}")
    private String protocol;

    @Value("${emailSender.host}")
    private String host;

    @Value("${emailSender.port}")
    private Integer port;

    @Value("${emailSender.username}")
    private String username;

    @Value("${emailSender.password}")
    private String password;

}
