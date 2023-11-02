package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.dao.VerificationCodeDao;
import it.pagopa.pn.externalchannels.dao.VerificationCodeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class VerificationCodeService {
    private final VerificationCodeDao verificationCodeDao;

    public VerificationCodeService(VerificationCodeDao verificationCodeDao) {
        this.verificationCodeDao = verificationCodeDao;
    }

    public void saveVerificationCode(String eventType, String messageText, String receiverDigitalAddress) {
        log.info("Start saveVerificationCode eventType: {} receiverDigitalAddress: {}",messageText,receiverDigitalAddress);
        if("VerificationCode".equals(eventType)){
            String verificationCode = getVerificationCodeFromHtml(messageText);
            log.info("Verification code is {} for address {}",verificationCode, receiverDigitalAddress);

            VerificationCodeEntity verificationCodeEntity = new VerificationCodeEntity();
            verificationCodeEntity.setVerificationCode(verificationCode);
            verificationCodeEntity.setPk(receiverDigitalAddress);
            
            verificationCodeDao.addVerificationCode(verificationCodeEntity);
        }
    }

    private String getVerificationCodeFromHtml(String messageText) {
        final String inserisciSuSendIlCodiceString = "inserisci su SEND il codice";
        int startIndex = messageText.indexOf(inserisciSuSendIlCodiceString) + inserisciSuSendIlCodiceString.length();
        int endIndex = messageText.indexOf("</h5>");

        String subtext = messageText.substring(startIndex , endIndex);
        return subtext.substring(subtext.length() - 5);
    }
    
    public Optional<VerificationCodeEntity> getVerificationCodeFromDb(String digitalAddress){
        return verificationCodeDao.getVerificationCode(digitalAddress);
    }
}
