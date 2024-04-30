package it.pagopa.pn.externalchannels.dao;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@DynamoDbBean
@NoArgsConstructor
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public class ReceivedMessagePaperEngageRequestEntity {


    @Getter(onMethod = @__({@DynamoDbAttribute("iun")}))
    private String iun;


    @Getter(onMethod = @__({@DynamoDbAttribute("requestId")}))
    private String requestId;


    @Getter(onMethod = @__({@DynamoDbAttribute("requestPaId")}))
    private String requestPaId;


    @Getter(onMethod = @__({@DynamoDbAttribute("clientRequestTimeStamp")}))
    private OffsetDateTime clientRequestTimeStamp;


    @Getter(onMethod = @__({@DynamoDbAttribute("productType")}))
    private String productType;


    @Getter(onMethod = @__({@DynamoDbAttribute("attachments")}))
    private List<ReceivedMessagePaperEngageRequestAttachmentEntity> attachments;


    @Getter(onMethod = @__({@DynamoDbAttribute("printType")}))
    private String printType;


    @Getter(onMethod = @__({@DynamoDbAttribute("receiverName")}))
    private String receiverName;


    @Getter(onMethod = @__({@DynamoDbAttribute("receiverNameRow2")}))
    private String receiverNameRow2;


    @Getter(onMethod = @__({@DynamoDbAttribute("receiverAddress")}))
    private String receiverAddress;


    @Getter(onMethod = @__({@DynamoDbAttribute("receiverAddressRow2")}))
    private String receiverAddressRow2;


    @Getter(onMethod = @__({@DynamoDbAttribute("receiverCap")}))
    private String receiverCap;


    @Getter(onMethod = @__({@DynamoDbAttribute("receiverCity")}))
    private String receiverCity;


    @Getter(onMethod = @__({@DynamoDbAttribute("receiverCity2")}))
    private String receiverCity2;


    @Getter(onMethod = @__({@DynamoDbAttribute("receiverPr")}))
    private String receiverPr;


    @Getter(onMethod = @__({@DynamoDbAttribute("receiverCountry")}))
    private String receiverCountry;


    @Getter(onMethod = @__({@DynamoDbAttribute("receiverFiscalCode")}))
    private String receiverFiscalCode;


    @Getter(onMethod = @__({@DynamoDbAttribute("senderName")}))
    private String senderName;


    @Getter(onMethod = @__({@DynamoDbAttribute("senderAddress")}))
    private String senderAddress;


    @Getter(onMethod = @__({@DynamoDbAttribute("senderCity")}))
    private String senderCity;


    @Getter(onMethod = @__({@DynamoDbAttribute("senderPr")}))
    private String senderPr;


    @Getter(onMethod = @__({@DynamoDbAttribute("senderDigitalAddress")}))
    private String senderDigitalAddress;


    @Getter(onMethod = @__({@DynamoDbAttribute("arName")}))
    private String arName;


    @Getter(onMethod = @__({@DynamoDbAttribute("arAddress")}))
    private String arAddress;


    @Getter(onMethod = @__({@DynamoDbAttribute("arCap")}))
    private String arCap;


    @Getter(onMethod = @__({@DynamoDbAttribute("arCity")}))
    private String arCity;

}


