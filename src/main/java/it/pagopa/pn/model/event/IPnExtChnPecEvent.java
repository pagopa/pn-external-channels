/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.model.event;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 *
 * @author GIANGR40
 */
public interface IPnExtChnPecEvent {

    // Header Section    
    @Schema(description = "Unique identifier of the Publisher.",
            example = "pn-delivery-push", required = true)
    public String getPublisher();

    @Schema(description = "Unique identifier of this message.",
            example = "c16997de-d567-479d-8b8e-bd4465e57054", required = true)
    public String getMessageId();

    @Schema(description = "The Message Type.",
            example = "PN-EXT_CHN-PEC", required = true)
    public String getMessageType();

    @Schema(description = "The Partition Key for serializing messages. Could be the IUN.",
            example = "IUN-ID", required = true)
    public String getPartitionKey();

    // Payload Section    
    @Schema(description = "The Callback or the Output topic/queue.",
            example = "IUN-ID", required = true)
    public String getCallbackUrl();

    @Schema(description = "Basic Description of Codice Atto.",
            example = "Codice Atto", required = true)
    public String getCodiceAtto();

    @Schema(description = "Basic Description of Numero Cronologico.",
            example = "Numero Cronologico", required = false)
    public String getNumeroCronologico();

    @Schema(description = "Basic Description of Parte Istante.",
            example = "Parte Istante", required = false)
    public String getParteIstante();

    @Schema(description = "Basic Description of Procuratore.",
            example = "Procuratore", required = false)
    public String getProcuratore();

    @Schema(description = "Basic Description of Ufficiale Giudiziario.",
            example = "Ufficiale Giudiziario", required = false)
    public String getUfficialeGiudiziario();

    @Schema(description = "Basic Description of IUN.",
            example = "Ufficiale Giudiziario", required = true)
    public String getIun();

    @Schema(description = "Basic Description of PA Mittente.",
            example = "PA Mittente", required = true)
    public String getPaMittente();

    @Schema(description = "Basic Description of PEC Mittente.",
            example = "PEC Mittente", required = true)
    public String getPecMittente();

    @Schema(description = "Basic Description of Destinatario.",
            example = "Destinatario", required = true)
    public String getDestinatario();

    @Schema(description = "Basic Description of Codice Fiscale.",
            example = "Codice Fiscale", required = true)
    public String getCodiceFiscale();

    @Schema(description = "Basic Description of PEC.",
            example = "PEC", required = true)
    public String getPec();

}
