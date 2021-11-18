package it.pagopa.pn.externalchannels.util;


import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.service.MessageBodyType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@Component
@Slf4j
public class MessageUtil {

	public static final String MSG_SUBJECT = "Avviso di avvenuta ricezione";
	
	private static final String HTML_MSG_TEMPLATE = "<p>Buongiorno Gentile ${recipientDenomination}</p>"
			+ "	<p>Ti informiano che ti &egrave; stato notificato il seguente Atto:</p>"
			+ "	<p>Codice univoco (IUN):<br/>"
			+ "	<b>${iun}</b></p>"
			+ "	<p>PA Mittente:<br/>"
			+ "	<b>${senderDenomination}</b></p>"
			+ " <p>Data invio:<br/>"
			+ " <b>${shipmentDate?datetime.iso?string[\"dd-MM-yyyy\"]}</b></p>"
			+ "	<p>Puoi consultare l'Atto in uno dei seguenti modi:</p>"
			+ "	<p>- Accedendo con SPID o CIE (Carta d'Identit&agrave; Elettronica) a Piattaforma Notifiche al seguente indirizzo<br/>"
			+ "	<a href=\"https://www.piattaformanotifiche.gov.it\">https://www.piattaformanotifiche.gov.it</a></p>"
			+ "	<p>- Recandoti presso un qualsiasi Ufficio Postale, dove potrai ritirare una copia dell'Atto stampato, semplicemente comunicando "
			+ "	<b>il codice IUN</b> di riferimento o mostrando il <b>Codice a Barre Univoco</b> allegato a questa comunicazione.</p>"
			+ "	<br/>"
			+ "	<p>Ti informiamo che in alternativa &egrave; anche possibile accedere temporaneamente all'Atto per un massimo di <b>2 accessi</b>,"
			+ "	attraverso l'url univoco riportato qui sotto.</p>"
			+ "	<p>URL UNIVOCO<br/><a href=\"${accessUrl}\">${accessUrl}</a></p>"
			+ "	<br/>"
			+ "	<p><i>Questo messaggio &egrave; stato inoltrato da un indirizzo di Posta Elettronica Certificata non abilitato a ricevere messaggi.<br/>"
			+ "	La invitiamo pertanto a non rispondere a questa comunicazione.<i></p>";

	private static final String TEXT_MSG_TEMPLATE = "Gentile ${recipientDenomination}\n"
			+ "\n"
			+ "La informiano che ti è stato notificato il seguente Atto:\n"
			+ "\n"
			+ "Codice univoco (IUN):\n"
			+ "${iun}\n"
			+ "\n"
			+ "PA Mittente:\n"
			+ "${senderDenomination}\n"
			+ "\n"
			+ "Data invio:\n"
			+ "${shipmentDate?datetime.iso?string[\"dd-MM-yyyy\"]}\n"
			+ "\n"
			+ "Puoi consultare l'Atto in uno dei seguenti modi:\n"
			+ " - Accedendo con SPID o CIE (Carta d'Identità Elettronica) a Piattaforma Notifiche al seguente indirizzo\n"
			+ "   https://www.piattaformanotifiche.gov.it\n"
			+ " - Recandoti presso un qualsiasi Ufficio Postale, dove potrai ritirare una copia dell'Atto stampato,"
			+ "   semplicemente comunicando il codice IUN di riferimento o mostrando il Codice a Barre Univoco allegato "
			+ "   a questa comunicazione.\n"
			+ "\n"
			+ "Ti informiamo che in alternativa è anche possibile accedere temporaneamente all'Atto per un massimo di 2 accessi, "
			+ "attraverso l'url univoco riportato qui sotto.\n"
			+ "\n"
			+ "URL UNIVOCO\n"
			+ "${accessUrl}\n"
			+ "\n"
			+ "Questo messaggio è stato inoltrato da un indirizzo di Posta Elettronica non abilitato a ricevere messaggi.\n"
			+ "La invitiamo pertanto a non rispondere a questa comunicazione.";

	Configuration freeMarker;

	public MessageUtil(Configuration freeMarker) {
		this.freeMarker = freeMarker;
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		stringLoader.putTemplate(MessageBodyType.PLAIN_TEXT.name(), TEXT_MSG_TEMPLATE);
		stringLoader.putTemplate(MessageBodyType.HTML.name(), HTML_MSG_TEMPLATE);
		this.freeMarker.setTemplateLoader(stringLoader);
	}

	public <T> String prepareMessage(GenericEvent<StandardEventHeader, T> evt, MessageBodyType type) {
		StringWriter stringWriter = new StringWriter();
		try {
			Template template = freeMarker.getTemplate(type.name());
			template.process(toModel(evt.getPayload()), stringWriter);
		} catch (Exception e) {
			log.error("Could not process message template", e);
			e.printStackTrace();
		}
		return stringWriter.getBuffer().toString();
	}

	private Map<String, Object> toModel(Object o)
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		Map<String, Object> model = PropertyUtils.describe(o);
		return model;
	}

}
