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
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ArchivesUtil {

    public static final String SEVEN_ZIP_EXTENSION = ".7z";
    public static final String ZIP_EXTENSION = ".zip";
    public static final String TMP_FILE_PREFIX = "tmp_";
    public static final String BOL_FILE_NAME = "attachment_example";
    public static final String BOL_FILE_EXTENSION = ".bol";

    public static File createBolFile(NotificationProgress notificationProgress, Integer pages) {
        File outputFile = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource("/");
            outputFile = File.createTempFile(BOL_FILE_NAME, BOL_FILE_EXTENSION, classPathResource.getFile());
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(String.format("attachment_example_%d_pages.pdf|||%s|||%s||||||||||||||||||||||||", pages, notificationProgress.getRequestId(), notificationProgress.getRegisteredLetterCode()).getBytes());
            }
            return outputFile;
        } catch (Exception e) {
            log.error("Error creating bol file", e);
            deleteFile(outputFile);
            throw new ExternalChannelsMockException("Error creating bol file", e);
        }
    }

    public static byte[] createZip(Integer pages, File bolFile) {
        try {
            ClassPathResource examplePdf = new ClassPathResource("attachment_example_" + pages + "_pages.pdf");
            final List<File> srcFiles = Arrays.asList(examplePdf.getFile(), bolFile);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
                for (File file : srcFiles) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(fis.readAllBytes());
                    }
                }
            }
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error creating zip file", e);
            throw new ExternalChannelsMockException("Error creating zip file", e);
        }
    }

    public static byte[] create7Zip(Integer pages, File bolFile) {
        File outputFile = null;
        try {
            ClassPathResource examplePdf = new ClassPathResource("attachment_example_" + pages + "_pages.pdf");
            final List<File> srcFiles = Arrays.asList(examplePdf.getFile(), bolFile);

            ClassPathResource classPathResource = new ClassPathResource("/");
            outputFile = File.createTempFile(TMP_FILE_PREFIX + UUID.randomUUID(), SEVEN_ZIP_EXTENSION, classPathResource.getFile());
            try (SevenZOutputFile outArchive = new SevenZOutputFile(outputFile)) {
                outArchive.setContentCompression(SevenZMethod.LZMA);
                outArchive.setContentMethods(List.of(new SevenZMethodConfiguration(SevenZMethod.LZMA)));
                for (File srcFile : srcFiles) {
                    final SevenZArchiveEntry entry = new SevenZArchiveEntry();
                    entry.setName(srcFile.getName());
                    outArchive.putArchiveEntry(entry);
                    try (FileInputStream fis = new FileInputStream(srcFile)) {
                        outArchive.write(fis.readAllBytes());
                    }
                    outArchive.closeArchiveEntry();
                }
            }
            return Files.readAllBytes(outputFile.toPath());
        } catch (Exception e) {
            log.error("Error creating 7zip file", e);
            deleteFile(outputFile);
            throw new ExternalChannelsMockException("Error creating 7zip file", e);
        }
    }

    public static File create7Zip(byte[] data) {
        File outputFile = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource("/");
            outputFile = File.createTempFile(TMP_FILE_PREFIX + UUID.randomUUID(), SEVEN_ZIP_EXTENSION, classPathResource.getFile());
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
            deleteFile(outputFile);
            throw new ExternalChannelsMockException("Error creating 7zip file from p7m", e);
        }
    }

    public static File createZip(byte[] data) {
        File outputFile = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource("/");
            outputFile = File.createTempFile(TMP_FILE_PREFIX + UUID.randomUUID(), ZIP_EXTENSION, classPathResource.getFile());
            FileOutputStream fos = new FileOutputStream(outputFile);
            try (ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                try (ByteArrayInputStream fis = new ByteArrayInputStream(data)) {
                    ZipEntry zipEntry = new ZipEntry("attachment_example.zip.p7m");
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                }
            }
            return outputFile;
        } catch (Exception e) {
            log.error("Error creating zip file from p7m", e);
            deleteFile(outputFile);
            throw new ExternalChannelsMockException("Error creating zip file from p7m", e);
        }
    }

    public static byte[] createP7mFile(byte[] data) {
        ClassPathResource keyFile = new ClassPathResource("key/private.key");
        ClassPathResource certFile = new ClassPathResource("key/certificate.crt");
        try {
            PEMParser pemParser = new PEMParser(new StringReader(Files.readString(Paths.get(keyFile.getURI()))));
            final PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject();
            final byte[] encoded = pemKeyPair.getPrivateKeyInfo().getEncoded();
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(encoded));

            InputStream targetStream = new ByteArrayInputStream((Files.readAllBytes(Paths.get(certFile.getURI()))));
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

    public static void deleteFile(File fileToDelete) {
        Optional.of(fileToDelete).ifPresent(file -> {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                throw new ExternalChannelsMockException("Error deleting temp file", e);
            }
        });
    }
}
