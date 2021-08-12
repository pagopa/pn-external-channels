/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.model.event;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 *
 * @author GIANGR40
 */
public interface IPnExtChnProgressStatusEvent {

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

    @Schema(description = "Basic Description of Codice Atto.",
            example = "Codice Atto", required = true)
    public String getCodiceAtto();

    @Schema(description = "Basic Description of IUN Code.",
            example = "IUN", required = true)
    public String getIun();

    @Schema(description = "Basic Description of Tipo Invio.",
            example = "Tipo Invio", required = true)
    public String getTipoInvio();

    @Schema(description = "Basic Description of Codice Raccomandata.",
            example = "Codice Raccomandata", required = false)
    public String getCodiceRaccomandata();

    @Schema(description = "Basic Description of ID Pec.",
            example = "ID Pec", required = false)
    public String getIDPec();

    @Schema(description = "Basic Description of Ricevuta EML Invio.",
            example = "Ricevuta EML Invio", required = false)
    public String getRicevutaEMLInvio();

    @Schema(description = "Basic Description of Ricevuta EML Consegna.",
            example = "Ricevuta EML Consegna", required = false)
    public String getRicevutaEMLConsegna();

    @Schema(description = "Basic Description of Stato.",
            example = "Stato", required = true)
    public String getStato();

    @Schema(description = "Basic Description of Data Stato.",
            example = "2021-08-03T18:01:07", required = true)
    public LocalDateTime getDataStato();

    @Schema(description = "Basic Description of Tentativo.",
            example = "2", required = false)
    public Integer getTentativo();

    @Schema(description = "Basic Description of Canale.",
            example = "Canale", required = false)
    public String getCanale();

}
