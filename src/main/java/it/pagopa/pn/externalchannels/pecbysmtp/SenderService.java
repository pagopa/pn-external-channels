package it.pagopa.pn.externalchannels.pecbysmtp;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class SenderService {

    private final PecMetadataDao dao;
    private final PecBySmtpCfg cfg;

    public SenderService(PecMetadataDao dao, PecBySmtpCfg cfg) {
        this.dao = dao;
        this.cfg = cfg;
    }

    private Session mailSession;
    private Transport smtpTransport;

    protected synchronized void renewSmtpTransport() {
        if(StringUtils.isNotBlank( cfg.getUser())) {
            if( smtpTransport != null ) {
                log.info("Close SMTP Transport");
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
    }


    private Transport buildNewSmtpTransport() throws NoSuchProviderException {
        log.info("Initialize SMTP Transport to host={} and user={}", cfg.getSmtpsHost(), cfg.getUser());

        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.host", cfg.getSmtpsHost() );
        this.mailSession = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cfg.getUser(), cfg.getPassword());
            }});
        return mailSession.getTransport( "smtp" );
    }


    public synchronized void sendMessage( SimpleMessage dto ) {
        if(StringUtils.isNotBlank( cfg.getUser())) {
            try {
                renewSmtpTransport();
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
    }

    private void tryToSend(SimpleMessage dto) throws MessagingException {
        log.info("Send SMTP pec message for eventId={} to host={}", dto.getEventId(), cfg.getSmtpsHost());
        log.debug("Pec message eventId={} iun={} key={} subject={} destAddress={}",
                dto.getEventId(),
                dto.getIun(),
                dto.getKey(),
                dto.getSubject(),
                dto.getRecipientAddress()
            );

        String messageId = UUID.randomUUID().toString().replaceAll("-", "");
        log.debug("Pec message eventId={} messageId={}", dto.getEventId(), messageId);

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

        log.debug("Sending SMTP eventId={} message={}", dto.getEventId(), message );
        smtpTransport.send(message);
        log.debug("Sent SMTP pec message for eventId={}", dto.getEventId() );

        log.info("Saving metadata for eventId=" + dto.getEventId());
        dao.saveMessageMetadata( messageId, dto );
        log.info("Saved metadata for eventId=" + dto.getEventId());
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
