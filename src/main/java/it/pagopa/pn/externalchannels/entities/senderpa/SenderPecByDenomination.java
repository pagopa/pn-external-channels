package it.pagopa.pn.externalchannels.entities.senderpa;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@NoArgsConstructor
@Table(value = "sender_pec_by_denomination")
@Document(value = "sender_pec_by_denomination")
public class SenderPecByDenomination {

    @PrimaryKey
    @MongoId
    private String denomination;

    private String idPec;

    private String pec;

}
