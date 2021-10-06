package it.pagopa.pn.externalchannels.arubapec.jmailutils;

import org.springframework.stereotype.Component;

import javax.mail.Store;

@Component
public class JMailUtils {

    public JMailStoreWrapper wrap(Store store) {
        return new JMailStoreWrapper( store );
    }
}
