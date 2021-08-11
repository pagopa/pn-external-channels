/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.event.eventinbound.pnextchnpecevent;

import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.pn.model.event.IPnExtChnPecEvent;
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
public class PnExtChnPecEvent implements IPnExtChnPecEvent {

    @JsonProperty("header")
    private PnExtChnPecEventHeader pnExtChnPecEventHeader;
    @JsonProperty("payload")
    private PnExtChnPecEventPayload pnExtChnPecEventPayload;

    // Header Section    
    
    @Override
    public String publisher() {
        return pnExtChnPecEventHeader.getPublisher();
    }

    @Override
    public String getMessageId() {
        return pnExtChnPecEventHeader.getMessageId();
    }

    @Override
    public String getMessageType() {
        return pnExtChnPecEventHeader.getMessageType();
    }

    @Override
    public String getPartitionKey() {
        return pnExtChnPecEventHeader.getPartitionKey();
    }
    
    // Payload Section

    @Override
    public String getCallbackUrl() {
        return pnExtChnPecEventPayload.getCallbackUrl();
    }

    @Override
    public String getCodiceAtto() {
        return pnExtChnPecEventPayload.getCodiceAtto();
    }

    @Override
    public String getNumeroCronologico() {
        return pnExtChnPecEventPayload.getNumeroCronologico();
    }

    @Override
    public String getParteIstante() {
        return pnExtChnPecEventPayload.getParteIstante();
    }

    @Override
    public String getProcuratore() {
        return pnExtChnPecEventPayload.getProcuratore();
    }

    @Override
    public String getUfficialeGiudiziario() {
        return pnExtChnPecEventPayload.getUfficialeGiudiziario();
    }

    @Override
    public String getIun() {
        return pnExtChnPecEventPayload.getIun();
    }

    @Override
    public String getPaMittente() {
        return pnExtChnPecEventPayload.getPaMittente();
    }

    @Override
    public String getPecMittente() {
        return pnExtChnPecEventPayload.getPecMittente();
    }

    @Override
    public String getDestinatario() {
        return pnExtChnPecEventPayload.getDestinatario();
    }

    @Override
    public String getCodiceFiscale() {
        return pnExtChnPecEventPayload.getCodiceFiscale();
    }

    @Override
    public String getPec() {
        return pnExtChnPecEventPayload.getPec();
    }
        
}
