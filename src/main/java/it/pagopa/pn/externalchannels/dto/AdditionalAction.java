package it.pagopa.pn.externalchannels.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalAction implements Serializable  {

    public enum ADDITIONAL_ACTIONS {
        DISCOVERY,
        DOC,
        DELAY,
        DELAYDOC
    }

    private ADDITIONAL_ACTIONS action;
    private String info;
}
