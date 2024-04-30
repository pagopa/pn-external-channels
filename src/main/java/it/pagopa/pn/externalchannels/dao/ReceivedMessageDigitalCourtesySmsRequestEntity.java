package it.pagopa.pn.externalchannels.dao;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.time.OffsetDateTime;

@Data
@DynamoDbBean
@NoArgsConstructor
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public class ReceivedMessageDigitalCourtesySmsRequestEntity {


    @Getter(onMethod = @__({@DynamoDbAttribute("requestId")}))
    private String requestId;

    @Getter(onMethod = @__({@DynamoDbAttribute("correlationId")}))
    private String correlationId;

    @Getter(onMethod = @__({@DynamoDbAttribute("eventType")}))
    private String eventType;

    @Getter(onMethod = @__({@DynamoDbAttribute("qos")}))
    private String qos;

    @Getter(onMethod = @__({@DynamoDbAttribute("clientRequestTimeStamp")}))
    private OffsetDateTime clientRequestTimeStamp;

    @Getter(onMethod = @__({@DynamoDbAttribute("receiverDigitalAddress")}))
    private String receiverDigitalAddress;

    @Getter(onMethod = @__({@DynamoDbAttribute("messageText")}))
    private String messageText;

    @Getter(onMethod = @__({@DynamoDbAttribute("senderDigitalAddress")}))
    private String senderDigitalAddress;

    @Getter(onMethod = @__({@DynamoDbAttribute("channel")}))
    private String channel;

}


