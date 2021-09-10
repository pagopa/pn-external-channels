package it.pagopa.pn.externalchannels.entities.queuedmessage;

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
@Table(value = "queued_message")
@Document(value = "queued_message")
public class QueuedMessage {

    @PrimaryKey
    @MongoId
    private String id;

    private String eventStatus;

    private String requestCorrelationId;

    private String iun;

    private String senderId;

    private String senderDenomination;

    private String senderPecAddress;

    private String recipientDenomination;

    private String recipientTaxId;

    private String pecAddress;


    // FIXME: tradurre in inglese
    private String codiceAtto;
    private String numeroCronologico;
    private String parteIstante;
    private String procuratore;
    private String ufficialeGiudiziario;

}
