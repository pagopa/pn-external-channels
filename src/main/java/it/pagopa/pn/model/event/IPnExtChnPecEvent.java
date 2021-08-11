/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.model.event;

/**
 *
 * @author GIANGR40
 */
public interface IPnExtChnPecEvent {
    
    // Header Section    
    public String publisher();
    public String getMessageId();
    public String getMessageType();
    public String getPartitionKey();
            
    // Payload Section    
    public String getCallbackUrl();
    public String getCodiceAtto();
    public String getNumeroCronologico();
    public String getParteIstante();
    public String getProcuratore();
    public String getUfficialeGiudiziario();
    public String getIun();
    public String getPaMittente();
    public String getPecMittente();        
    public String getDestinatario();
    public String getCodiceFiscale();
    public String getPec();
    
}
