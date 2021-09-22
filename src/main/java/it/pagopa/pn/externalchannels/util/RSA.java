package it.pagopa.pn.externalchannels.util;

import org.apache.tomcat.util.codec.binary.Base64;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;

public class RSA {

    private RSA(){

    }

    public static X509Certificate getCertificateFromPem(String pem) throws IOException, GeneralSecurityException {
        pem = "-----BEGIN CERTIFICATE-----\n" + pem + "\n-----END CERTIFICATE-----";
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        InputStream is = new ByteArrayInputStream(pem.getBytes());
        X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
        is.close();
        return cer;
    }

    public static boolean verify(PublicKey publicKey, String message, String signature) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        Signature sign = Signature.getInstance("SHA1withRSA");
        sign.initVerify(publicKey);
        sign.update(message.getBytes(StandardCharsets.UTF_8));
        return sign.verify(Base64.decodeBase64(signature.getBytes(StandardCharsets.UTF_8)));
    }

    public static String encrypt(String rawText, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.encodeBase64String(cipher.doFinal(rawText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
