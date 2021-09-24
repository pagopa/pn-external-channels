package it.pagopa.pn.externalchannels;

import it.pagopa.pn.commons.configs.PnAutoConfigurationSelector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({PnAutoConfigurationSelector.class})
public class Application {

    public static void main(String[] args) {        
        SpringApplication.run(Application.class, args);
    }
}
