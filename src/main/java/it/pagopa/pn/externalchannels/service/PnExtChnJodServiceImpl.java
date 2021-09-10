package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.jod.wsclient.*;
import it.pagopa.pn.externalchannels.pojos.JodAuth;
import it.pagopa.pn.externalchannels.util.RSA;
import it.pagopa.pn.externalchannels.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "file-transfer-service.implementation", havingValue = "jod")
public class PnExtChnJodServiceImpl implements PnExtChnFileTransferService {

    @Autowired
    PnExtChnJodClient jodClient;

    private X509Certificate jodCertificate;

    @Value("${jod-ft.auth.username}")
    private String username;

    @Value("${jod-ft.auth.password}")
    private String password;

    @Value("${jod-ft.url.metadata}")
    private String metadataUrl;

    @Value("${jod-ft.filepart-size:0}")
    private Integer filepartSize;

    @PostConstruct
    private void initSecurity() throws GeneralSecurityException, IOException {
        String pem = Util.getXmlTagValue(metadataUrl, "/metadata/certificate");
        jodCertificate = RSA.getCertificateFromPem(pem);
    }

    @Override
    public void transferCsv(byte[] csv) {
        //STUB
        log.info("JodServiceImpl - transferCsv - START");

        InitUploadRequest initRequest = createInitUploadRequest(csv);
        IdUploadResponse initRes = jodClient.initUpload(initRequest, createJodAuth());

        List<byte[]> chunks = Util.splitByteArray(csv, filepartSize);

        for (int i = 0; i < chunks.size(); i++) {
            byte[] chunk = chunks.get(i);
            FilePartUploadRequest filePartRequest = createFilePartUploadRequest(initRes, chunk, i+1);
            jodClient.filePartUpload(filePartRequest, createJodAuth());
        }

        EndUploadRequest endRequest = createEndUploadRequest(initRes);
        jodClient.endFileUpload(endRequest, createJodAuth());

        log.info("JodServiceImpl - transferCsv - END");
    }

    private JodAuth createJodAuth() {
        JodAuth jodAuth = new JodAuth();
        jodAuth.setUsername(username);
        jodAuth.setPassword(cipherPassword());
        return jodAuth;
    }

    private String cipherPassword() {
        String nonce = UUID.randomUUID().toString();
        String b64crypt = RSA.encrypt(password, jodCertificate.getPublicKey());
        String unixTs = String.valueOf(Instant.now().getEpochSecond());
        String format = String.format("%s:%s:%s", nonce, b64crypt, unixTs);
        return Base64.getEncoder().encodeToString(format.getBytes(StandardCharsets.UTF_8));
    }

    private InitUploadRequest createInitUploadRequest(byte[] bytes) {
        String md5 = DigestUtils.md5DigestAsHex(bytes).toUpperCase();
        InitUploadRequest request = new InitUploadRequest();
        request.setChunckSize(String.valueOf(0 == filepartSize ? bytes.length : filepartSize));
        request.setMd5SumFile(md5);
        request.setFileName("");
        request.setCodiceCliente("");
        request.setCodiceServizio("");
        return request;
    }

    private FilePartUploadRequest createFilePartUploadRequest (IdUploadResponse idUpload, byte[] bytes, int index) {
        FilePartUploadRequest request = new FilePartUploadRequest();
        String md5 = DigestUtils.md5DigestAsHex(bytes).toUpperCase();
        request.setUuid(idUpload.getUuid());
        request.setFilePartIndex(index);
        request.setFilePartAsBase64(Base64.getEncoder().encodeToString(bytes));
        request.setMd5SumFilePartAsBase64(Base64.getEncoder().encodeToString(md5.getBytes(StandardCharsets.UTF_8)));
        return request;
    }

    private EndUploadRequest createEndUploadRequest(IdUploadResponse idUpload){
        EndUploadRequest request = new EndUploadRequest();
        request.setUuid(idUpload.getUuid());
        return request;
    }

}
