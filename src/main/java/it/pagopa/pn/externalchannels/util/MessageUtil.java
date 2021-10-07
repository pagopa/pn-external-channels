package it.pagopa.pn.externalchannels.util;


import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

import org.springframework.stereotype.Component;

import it.pagopa.pn.api.dto.events.PnExtChnEmailEventPayload;
import it.pagopa.pn.api.dto.events.PnExtChnPecEventPayload;
import it.pagopa.pn.externalchannels.service.MessageBodyType;

@Component
public class MessageUtil {

    public static final String MSG_SUBJECT = "Avviso di avvenuta ricezione";

    private static final String HTML_MSG_TEMPLATE = "<p>Buongiorno Gentile %s</p>"
            + "	<p>Ti informiano che ti &egrave; stato notificato il seguente Atto Giudiziario:</p>"
            + "	<p>Codice univoco (IUN):<br/>"
            + "	<b>%s</b></p>"
            + "	<p>PA Mittente:<br/>"
            + "	<b>%s</b></p>"
            + " <p>Data invio:<br/>"
            + " <b>%s</b></p>"
            + "	<p>Puoi consultare l'Atto in uno dei seguenti modi:</p>"
            + "	<p>- Accedendo con SPID o CIE (Carta d'Identit&agrave; Elettronica) a Piattaforma Notifiche al seguente indirizzo<br/>"
            + "	<a href=\"https://www.piattaformanotifiche.gov.it\">https://www.piattaformanotifiche.gov.it</a></p>"
            + "	<p>- Recandoti presso un qualsiasi Ufficio Postale, dove potrai ritirare una copia dell'Atto stampato, semplicemente comunicando "
            + "	<b>il codice IUN</b> di riferimento o mostrando il <b>Codice a Barre Univoco</b> allegato a questa comunicazione.</p>"
            + "	<br/>"
            + "	<p>Ti informiamo che in alternativa &egrave; anche possibile accedere temporaneamente all'Atto per un massimo di <b>2 accessi</b>,"
            + "	attraverso l'url univoco riportato qui sotto.</p>"
            + "	<p>URL UNIVOCO<br/><a href=\"https://at%s.gov.it\">https://at%s.gov.it</a></p>"
            + "	<br/>"
            + "	<p><i>Questo messaggio &egrave; stato inoltrato da un indirizzo di Posta Elettronica Certificata non abilitato a ricevere messaggi.<br/>"
            + "	La invitiamo pertanto a non rispondere a questa comunicazione.<i></p>";

    private static final String TEXT_MSG_TEMPLATE = "Buongiorno Gentile %s\n\n"
            + "Ti informiano che ti è stato notificato il seguente Atto Giudiziario:\n\n"
            + "Codice univoco (IUN):\n"
            + "%s\n\n"
            + "PA Mittente:\n"
            + "%s\n\n"
            + "Data invio:\n"
            + "%s\n\n"
            + "Puoi consultare l'Atto in uno dei seguenti modi:\n\n"
            + "- Accedendo con SPID o CIE (Carta d'Identità Elettronica) a Piattaforma Notifiche al seguente indirizzo\n"
            + "https://www.piattaformanotifiche.gov.it\n\n"
            + "Recandoti presso un qualsiasi Ufficio Postale, dove potrai ritirare una copia dell'Atto stampato, semplicemente comunicando "
            + "il codice IUN di riferimento o mostrando il Codice a Barre Univoco allegato a questa comunicazione.\n\n"
            + "Ti informiamo che in alternativa è anche possibile accedere temporaneamente all'Atto per un massimo di 2 accessi, "
            + "attraverso l'url univoco riportato qui sotto.\n\n"
            + "URL UNIVOCO\n"
            + "https://at%s.gov.it\n\n"
            + "Questo messaggio è stato inoltrato da un indirizzo di Posta Elettronica Certificata non abilitato a ricevere messaggi.\n"
            + "La invitiamo pertanto a non rispondere a questa comunicazione.";

    public String pecPayloadToMessage (PnExtChnPecEventPayload payload, MessageBodyType type) {
        if ( type.equals(MessageBodyType.HTML ) ) {
            return pecPayloadToHtmlBody( payload );
        } else {
            return pecPayloadToPlainTextBody( payload );
        }
    }

    public String mailPayloadToMessage (PnExtChnEmailEventPayload payload, MessageBodyType type) {
        if ( type.equals(MessageBodyType.HTML ) ) {
            return emailPayloadToHtmlBody( payload );
        } else {
            return emailPayloadToPlainTextBody( payload );
        }
    }

    private String pecPayloadToHtmlBody( PnExtChnPecEventPayload payload ) {
        return String.format( HTML_MSG_TEMPLATE, payload.getRecipientDenomination(),
                payload.getIun(),
                payload.getSenderDenomination(),
                convertShipmentDateToString(payload.getShipmentDate()),
                payload.getIun(),
                payload.getIun() );
    }

    private String pecPayloadToPlainTextBody( PnExtChnPecEventPayload payload ) {
        return String.format( TEXT_MSG_TEMPLATE, payload.getRecipientDenomination(),
                payload.getIun(),
                payload.getSenderDenomination(),
                convertShipmentDateToString(payload.getShipmentDate()),
                payload.getIun(),
                payload.getIun() );
    }

    private String emailPayloadToHtmlBody( PnExtChnEmailEventPayload payload ) {
        return String.format( HTML_MSG_TEMPLATE, payload.getRecipientDenomination(),
                payload.getIun(),
                payload.getSenderDenomination(),
                convertShipmentDateToString(payload.getShipmentDate()),
                payload.getIun(),
                payload.getIun() );
    }

    private String emailPayloadToPlainTextBody( PnExtChnEmailEventPayload payload ) {
        return String.format( TEXT_MSG_TEMPLATE, payload.getRecipientDenomination(),
                payload.getIun(),
                payload.getSenderDenomination(),
                convertShipmentDateToString(payload.getShipmentDate()),
                payload.getIun(),
                payload.getIun() );
    }

    private String convertShipmentDateToString (Instant shipmentDate) {
        String parsedDate = null;

        if (shipmentDate != null) {
            OffsetDateTime odt = shipmentDate.atOffset(ZoneOffset.UTC);
            int year = odt.get(ChronoField.YEAR_OF_ERA);
            int month = odt.get(ChronoField.MONTH_OF_YEAR);
            int day = odt.get(ChronoField.DAY_OF_MONTH);
            parsedDate = String.format("%02d-%02d-%04d", day, month, year);
        }

        return parsedDate;
    }
}