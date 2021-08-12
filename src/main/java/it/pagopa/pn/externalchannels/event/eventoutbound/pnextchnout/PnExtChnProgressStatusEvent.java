/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.event.eventoutbound.pnextchnout;

import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.pn.model.event.IPnExtChnProgressStatusEvent;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author GIANGR40
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PnExtChnProgressStatusEvent implements IPnExtChnProgressStatusEvent {

    @JsonProperty("header")
    private PnExtChnProgressStatusEventHeader pnExtChnProgressStatusEventHeader;
    @JsonProperty("payload")
    private PnExtChnProgressStatusEventPayload pnExtChnProgressStatusEventPayload;

    // Header Section    
    @Override
    public String getPublisher() {
        return pnExtChnProgressStatusEventHeader.getPublisher();
    }

    @Override
    public String getMessageId() {
        return pnExtChnProgressStatusEventHeader.getMessageId();
    }

    @Override
    public String getMessageType() {
        return pnExtChnProgressStatusEventHeader.getMessageType();
    }

    @Override
    public String getPartitionKey() {
        return pnExtChnProgressStatusEventHeader.getPartitionKey();
    }

    // Payload Section
    @Override
    public String getCodiceAtto() {
        return pnExtChnProgressStatusEventPayload.getCodiceAtto();
    }

    @Override
    public String getIun() {
        return pnExtChnProgressStatusEventPayload.getIun();
    }

    @Override
    public String getTipoInvio() {
        return pnExtChnProgressStatusEventPayload.getTipoInvio();
    }

    @Override
    public String getCodiceRaccomandata() {
        return pnExtChnProgressStatusEventPayload.getCodiceRaccomandata();
    }

    @Override
    public String getIDPec() {
        return pnExtChnProgressStatusEventPayload.getIDPec();
    }

    @Override
    public String getRicevutaEMLInvio() {
        return pnExtChnProgressStatusEventPayload.getRicevutaEMLInvio();
    }

    @Override
    public String getRicevutaEMLConsegna() {
        return pnExtChnProgressStatusEventPayload.getRicevutaEMLConsegna();
    }

    @Override
    public String getStato() {
        return pnExtChnProgressStatusEventPayload.getStato();
    }

    @Override
    public LocalDateTime getDataStato() {
        return pnExtChnProgressStatusEventPayload.getDataStato();
    }

    @Override
    public Integer getTentativo() {
        return pnExtChnProgressStatusEventPayload.getTentativo();
    }

    @Override
    public String getCanale() {
        return pnExtChnProgressStatusEventPayload.getCanale();
    }

}
