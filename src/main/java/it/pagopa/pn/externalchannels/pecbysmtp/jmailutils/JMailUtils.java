package it.pagopa.pn.externalchannels.pecbysmtp.jmailutils;

import it.pagopa.pn.externalchannels.pecbysmtp.PecBySmtpCfg;
import org.springframework.stereotype.Component;

import javax.mail.Store;

@Component
public class JMailUtils {

    private final PecBySmtpCfg cfg;

    public JMailUtils(PecBySmtpCfg cfg) {
        this.cfg = cfg;
    }

    public JMailStoreWrapper wrap(Store store) {
        return new JMailStoreWrapper( store, cfg.getSmtpsFolder() );
    }
}
