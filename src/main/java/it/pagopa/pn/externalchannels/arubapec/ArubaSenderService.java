package it.pagopa.pn.externalchannels.arubapec;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;
import java.util.UUID;

@Service
@Slf4j
public class ArubaSenderService {

    private final PecMetadataDao dao;
    private final ArubaCfg cfg;

    public ArubaSenderService(PecMetadataDao dao, ArubaCfg cfg) {
        this.dao = dao;
        this.cfg = cfg;
        renewSmtpTransport();
    }

    private Session mailSession;
    private Transport smtpTransport;

    protected synchronized void renewSmtpTransport() {
        if( smtpTransport != null ) {
            try {
                smtpTransport.close();
            } catch (MessagingException exc) {
                this.log.error("Closing SMTP transport", exc);
            }
            smtpTransport = null;
        }
        try {
            smtpTransport = buildNewSmtpTransport();
        } catch (NoSuchProviderException exc) {
            throw new PnInternalException("Preparing SMTP transport", exc);
        }
    }


    private Transport buildNewSmtpTransport() throws NoSuchProviderException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.host", "smtp.pec.it");
        this.mailSession = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cfg.getUser(), cfg.getPassword());
            }});
        return mailSession.getTransport( "smtp" );
    }


    public synchronized void sendMessage( SimpleMessage dto ) {
        try {
            tryToSend(dto);
        }
        catch (MessagingException exc1) {
            log.error("Trying to send a pec", exc1);
            renewSmtpTransport();
            try {
                tryToSend(dto);
            } catch (MessagingException exc2) {
                throw new PnInternalException("Trying to send a pec", exc2);
            }
        }
    }

    private void tryToSend(SimpleMessage dto) throws MessagingException {

        String messageId = UUID.randomUUID().toString().replaceAll("-", "");

        dao.saveMessageMetadata( messageId, dto );

        Message message = new MimeMessageWithFixedId( mailSession, messageId );

        message.setFrom(new InternetAddress( cfg.getUser() ));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(dto.getRecipientAddress()));
        message.setSubject( dto.getSubject() );

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent( dto.getContent(), dto.getContentType() );

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);
        smtpTransport.send(message);
        log.info("Send PEC for " + dto);
    }

    private static class MimeMessageWithFixedId extends MimeMessage {

        private final String messageId;

        public MimeMessageWithFixedId(Session session, String messageId) {
            super(session);
            this.messageId = messageId;
        }

        protected void updateMessageID() throws MessagingException {
            setHeader("Message-ID", messageId);
        }
    }
}
