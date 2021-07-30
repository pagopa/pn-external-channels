package it.pagopa.pn.extchannels;

import it.pagopa.pn.commons.PnAutoConfigurationImportSelector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Import(PnAutoConfigurationImportSelector.class)
@SpringBootApplication(scanBasePackages = {"it.pagopa.pn.commons", "it.pagopa.pn.extchannels"})
@EnableScheduling
public class PnExtChannelsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PnExtChannelsApplication.class, args);
	}
        
}
