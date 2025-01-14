package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.dao.VerificationCodeDao;
import it.pagopa.pn.externalchannels.dao.VerificationCodeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class VerificationCodeService {
    private static final String VERIFICATION_CODE_REGEX = "il codice[\\s\\S]*?<h5[^>]*>\\s*(.*?)\\s*</h5>";
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
        Pattern pattern = Pattern.compile(VERIFICATION_CODE_REGEX);
        Matcher matcher = pattern.matcher(messageText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    public Optional<VerificationCodeEntity> getVerificationCodeFromDb(String digitalAddress){
        return verificationCodeDao.getVerificationCode(digitalAddress);
    }
}
