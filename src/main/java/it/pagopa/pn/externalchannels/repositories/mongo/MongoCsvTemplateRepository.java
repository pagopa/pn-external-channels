package it.pagopa.pn.externalchannels.repositories.mongo;

import it.pagopa.pn.externalchannels.entities.csvtemplate.CsvTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoCsvTemplateRepository extends MongoRepository<CsvTemplate, String> {

    CsvTemplate findFirstByIdCsv(String idCsv);

}
