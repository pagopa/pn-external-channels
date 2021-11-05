package it.pagopa.pn.externalchannels.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class JobProperties {

    @Value("${job.messages-csv-template-id}")
    private String messagesCsvTemplateId;

    @Value("${job.results-csv-template-id}")
    private String resultCsvTemplateId;

    @Value("${job.customer}")
    private Integer customer;

    @Value("${job.macroservice.digital}")
    private Integer digitalMacroservice;

    @Value("${job.macroservice.physical}")
    private Integer physicalMacroservice;

}
