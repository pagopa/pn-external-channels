package it.pagopa.pn.extrnalchannels.util;

import it.pagopa.pn.externalchannels.util.ArchivesUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test che serve a creare in locale uno zip firmato in p7m
 */
@Disabled
class ArchivesUtilTest {


    @Test
    void createP7mFileTest() throws IOException {

        ClassPathResource resource = new ClassPathResource("archivio.zip");

        byte[] inputBytes = resource.getInputStream().readAllBytes();
        byte[] p7mContent = ArchivesUtil.createP7mFile(inputBytes);

        //target/test-classes/archivio.zip.p7m
        Path outputPath = Paths.get(resource.getFile().getParent(), "archivio.zip.p7m");

        Files.write(outputPath, p7mContent);

        System.out.println("File p7m creato in: " + outputPath.toAbsolutePath());
        assertThat(Files.exists(outputPath)).isTrue();
    }
}
