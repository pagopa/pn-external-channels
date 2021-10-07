package it.pagopa.pn.externalchannels.entities.resultdescriptor;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "result_descriptor")
@Document(value = "result_descriptor")
public class ResultDescriptor {

    @PrimaryKey
    @MongoId
    private String code;

    private String serviceCode;

    private boolean positive;

    private boolean retryable;

}
