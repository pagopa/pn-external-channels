package it.pagopa.pn.externalchannels.util;

import it.pagopa.pn.externalchannels.dto.NotificationProgress;
import it.pagopa.pn.externalchannels.exception.ExternalChannelsMockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ArchivesUtil {

    public static final String SEVEN_ZIP_EXTENSION = ".7z";

    public static void createBolFile(NotificationProgress notificationProgress, Integer pages) {
        try (FileOutputStream fos = new FileOutputStream("src/main/resources/attachment_example.bol")) {
            fos.write(String.format("attachment_example_%d.pdf|||%s|||%s||||||||||||||||||||||||", pages, notificationProgress.getRequestId(), notificationProgress.getRegisteredLetterCode()).getBytes());
        } catch (Exception e) {
            log.error("Error creating bol file", e);
            throw new ExternalChannelsMockException("Error creating bol file", e);
        }
    }

    public static byte[] createZip(Integer pages) {
        ClassPathResource examplePdf = new ClassPathResource("attachment_example_" + pages + ".pdf");
        ClassPathResource exampleBol = new ClassPathResource("attachment_example.bol");
        final List<ClassPathResource> srcFiles = Arrays.asList(examplePdf, exampleBol);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
            for (ClassPathResource resFile : srcFiles) {
                try (FileInputStream fis = new FileInputStream(resFile.getFile())) {
                    ZipEntry zipEntry = new ZipEntry(resFile.getFile().getName());
                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(fis.readAllBytes());
                }
            }
        } catch (Exception e ) {
            log.error("Error creating zip file", e);
            throw new ExternalChannelsMockException("Error creating zip file", e);
        }
        return baos.toByteArray();
    }

    public static byte[] create7Zip(Integer pages) {
        String file1 = "src/main/resources/attachment_example_" + pages + ".pdf";
        String file2 = "src/main/resources/attachment_example.bol";
        final List<String> srcFiles = Arrays.asList(file1, file2);
        try {
            File outputFile = File.createTempFile(UUID.randomUUID().toString(), SEVEN_ZIP_EXTENSION);
            try (SevenZOutputFile outArchive = new SevenZOutputFile(outputFile)) {
                outArchive.setContentCompression(SevenZMethod.LZMA);
                outArchive.setContentMethods(List.of(new SevenZMethodConfiguration(SevenZMethod.LZMA)));
                for (String srcFile : srcFiles) {
                    File fileToZip = new File(srcFile);
                    final SevenZArchiveEntry entry = new SevenZArchiveEntry();
                    entry.setName(fileToZip.getName());
                    outArchive.putArchiveEntry(entry);
                    try (FileInputStream fis = new FileInputStream(fileToZip)) {
                        outArchive.write(fis.readAllBytes());
                    }
                    outArchive.closeArchiveEntry();
                }
            }
            return Files.readAllBytes(outputFile.toPath());
        } catch (Exception e) {
            log.error("Error creating 7zip file", e);
            throw new ExternalChannelsMockException("Error creating 7zip file", e);
        }
    }

    public static File create7Zip(byte[] data) {
        try {
            File outputFile = File.createTempFile(UUID.randomUUID().toString(), SEVEN_ZIP_EXTENSION);
            try (SevenZOutputFile outArchive = new SevenZOutputFile(outputFile)) {
                outArchive.setContentCompression(SevenZMethod.LZMA);
                outArchive.setContentMethods(List.of(new SevenZMethodConfiguration(SevenZMethod.LZMA)));
                final SevenZArchiveEntry entry = new SevenZArchiveEntry();
                entry.setName("attachment_example.7z.p7m");
                outArchive.putArchiveEntry(entry);
                outArchive.write(data);
                outArchive.closeArchiveEntry();
            }
            return outputFile;
        } catch (Exception e) {
            log.error("Error creating 7zip file from p7m", e);
            throw new ExternalChannelsMockException("Error creating 7zip file from p7m", e);
        }
    }

    public static void createZip(byte[] data) {
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream("src/main/resources/attachment_example_completed.zip"))) {
            try (ByteArrayInputStream fis = new ByteArrayInputStream(data)) {
                ZipEntry zipEntry = new ZipEntry("attachment_example.zip.p7m");
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
            }
        } catch (Exception e) {
            log.error("Error creating zip file from p7m", e);
            throw new ExternalChannelsMockException("Error creating zip file from p7m", e);
        }
    }

    public static byte[] createP7mFile(byte[] data) {
        try {
            PEMParser pemParser = new PEMParser(new StringReader(Files.readString(Paths.get("src/main/resources/private.key"))));
            final PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject();
            final byte[] encoded = pemKeyPair.getPrivateKeyInfo().getEncoded();
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(encoded));

            InputStream targetStream = new ByteArrayInputStream((Files.readAllBytes(Paths.get("src/main/resources/certificate.crt"))));
            X509Certificate cert = (X509Certificate) CertificateFactory
                    .getInstance("X509")
                    .generateCertificate(targetStream);
            Store<?> certs = new JcaCertStore(Collections.singletonList(cert));

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            DigestCalculatorProvider digestCalculatorProvider = new JcaDigestCalculatorProviderBuilder().build();
            JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder("SHA256withRSA");
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(digestCalculatorProvider)
                    .build(contentSignerBuilder.build(privateKey), cert));

            gen.addCertificates(certs);

            CMSTypedData msg = new CMSProcessableByteArray(data);
            CMSSignedData signedData = gen.generate(msg, true);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                baos.write(signedData.getEncoded());
                return baos.toByteArray();
            }
        } catch (Exception e) {
            log.error("Error creating p7m file", e);
            throw new ExternalChannelsMockException("Error creating p7m file", e);
        }
    }
}
