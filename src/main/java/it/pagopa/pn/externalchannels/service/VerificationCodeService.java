package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.dao.VerificationCodeDao;
import it.pagopa.pn.externalchannels.dao.VerificationCodeEntity;
import it.pagopa.pn.externalchannels.model.DigitalNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VerificationCodeService {
    private final VerificationCodeDao verificationCodeDao;

    public VerificationCodeService(VerificationCodeDao verificationCodeDao) {
        this.verificationCodeDao = verificationCodeDao;
    }

    public void saveVerificationCode(DigitalNotificationRequest request) {
        if(request.getEventType().equals("VerificationCode")){
            String verificationCode = getVerificationCode(request.getMessageText());
            log.info("Verification code is {} for address {}",verificationCode, request.getReceiverDigitalAddress());

            VerificationCodeEntity verificationCodeEntity = new VerificationCodeEntity();
            verificationCodeEntity.setVerificationCode(verificationCode);
            verificationCodeEntity.setPk(request.getReceiverDigitalAddress());
            
            verificationCodeDao.addVerificationCode(verificationCodeEntity);
        }
    }

    private String getVerificationCode(String messageText) {
        final String inserisciSuSendIlCodiceString = "inserisci su SEND il codice";
        int startIndex = messageText.indexOf(inserisciSuSendIlCodiceString) + inserisciSuSendIlCodiceString.length();
        int endIndex = messageText.indexOf("</h5>");

        String subtext = messageText.substring(startIndex , endIndex);
        return subtext.substring(subtext.length() - 5);
    }
}
