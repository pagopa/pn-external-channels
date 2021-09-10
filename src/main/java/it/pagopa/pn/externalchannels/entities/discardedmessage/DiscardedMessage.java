package it.pagopa.pn.externalchannels.entities.discardedmessage;

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
@Table(value = "discarded_message")
@Document(value = "discarded_message")
public class DiscardedMessage {

    @PrimaryKey
    @MongoId
    private String id;

    private String message;

    private List<String> reasons;

}
