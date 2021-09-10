package it.pagopa.pn.externalchannels.entities.csvtemplate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Table(value = "csv_template")
@Document(value = "csv_template")
public class CsvTemplate {

    @PrimaryKey
    @MongoId
    private String id;

    private String idCsv;

    private String descrizione;

    private String colonne;

}
