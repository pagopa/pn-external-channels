package it.pagopa.pn.externalchannels.dao;

import java.util.Optional;

public interface VerificationCodeDao {
    void addVerificationCode(VerificationCodeEntity verificationCodeEntity);

    Optional<VerificationCodeEntity> getVerificationCode(String pk);
}