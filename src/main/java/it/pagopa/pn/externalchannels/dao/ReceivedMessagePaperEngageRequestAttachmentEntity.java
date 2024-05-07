package it.pagopa.pn.externalchannels.dao;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.math.BigDecimal;

@DynamoDbBean
@NoArgsConstructor
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@ToString
public class ReceivedMessagePaperEngageRequestAttachmentEntity {

    @Getter(onMethod = @__({@DynamoDbAttribute("uri")}))
    private String uri;

    @Getter(onMethod = @__({@DynamoDbAttribute("order")}))
    private BigDecimal order;

    @Getter(onMethod = @__({@DynamoDbAttribute("documentType")}))
    private String documentType;

    @Getter(onMethod = @__({@DynamoDbAttribute("sha256")}))
    private String sha256;

}


