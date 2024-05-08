package it.pagopa.pn.externalchannels.dao;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.UpdateBehavior;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.Instant;

@Data
@DynamoDbBean
@NoArgsConstructor
@ToString
public class ReceivedMessageEntity {
    public static final String GSI_INDEX_IUN = "iun-gsi";

    public static final String SEPARATORE = "#";

    public static final String COL_PK = "requestId";
    public static final String COL_IUNRECINDEX = "iunRecIndex";
    public static final String COL_CREATED = "created";
    public static final String COL_TTL = "ttl";

    public static final String COL_DigitalNotificationRequest = "digitalNotificationRequest";
    public static final String COL_DigitalCourtesySmsRequest = "digitalCourtesySmsRequest";
    public static final String COL_DigitalCourtesyMailRequest = "digitalCourtesyMailRequest";
    public static final String COL_PaperEngageRequest = "paperEngageRequest";


    public ReceivedMessageEntity(String iun, String recipientIndex){
        this.setCreated(Instant.now());
        if (iun != null)
        {
            this.setIunRecIndex(iun + (recipientIndex != null?(SEPARATORE+recipientIndex):""));
        }
    }

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)})) private String pk;
    @Getter(onMethod=@__({@DynamoDbSecondaryPartitionKey(indexNames = { GSI_INDEX_IUN}), @DynamoDbAttribute(COL_IUNRECINDEX)})) private String iunRecIndex;
    @Setter
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_CREATED), @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_IF_NOT_EXISTS)}))  private Instant created;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_TTL)}))  private long ttl;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DigitalNotificationRequest)})) private ReceivedMessageDigitalNotificationRequestEntity digitalNotificationRequest;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DigitalCourtesySmsRequest)})) private ReceivedMessageDigitalCourtesySmsRequestEntity digitalCourtesySmsRequest;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DigitalCourtesyMailRequest)})) private ReceivedMessageDigitalCourtesyMailRequestEntity digitalCourtesyMailRequest;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_PaperEngageRequest)})) private ReceivedMessagePaperEngageRequestEntity paperEngageRequest;
}