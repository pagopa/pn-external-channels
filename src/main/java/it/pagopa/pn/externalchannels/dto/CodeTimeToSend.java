package it.pagopa.pn.externalchannels.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class CodeTimeToSend implements Serializable {


    @Getter(onMethod=@__({@DynamoDbAttribute("code")})) private String code;
    @Getter(onMethod=@__({@DynamoDbAttribute("time")})) private Duration time;
    @Getter(onMethod=@__({@DynamoDbAttribute("additionalActions")})) private List<AdditionalAction> additionalActions;
    @Getter(onMethod=@__({@DynamoDbAttribute("disableAutoBusinessDatetime")})) private Boolean disableAutoBusinessDatetime = false;
}
