package it.pagopa.pn.externalchannels.repositories.cassandra;

import it.pagopa.pn.externalchannels.entities.csvtemplate.CsvTemplate;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.repository.CrudRepository;

public interface CsvTemplateRepository extends CrudRepository<CsvTemplate, String> {

    @AllowFiltering
    CsvTemplate findFirstByIdCsv(String idCsv);

}
