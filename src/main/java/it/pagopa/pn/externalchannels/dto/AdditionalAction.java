package it.pagopa.pn.externalchannels.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class AdditionalAction implements Serializable  {

    public enum ADDITIONAL_ACTIONS {
        DISCOVERY,
        DOC,
        FAILCAUSE,
        DELAY,
        DELAYDOC,
        PAGES
    }

    @Getter(onMethod=@__({@DynamoDbAttribute("action")})) private ADDITIONAL_ACTIONS action;
    @Getter(onMethod=@__({@DynamoDbAttribute("info")})) private String info;
}
