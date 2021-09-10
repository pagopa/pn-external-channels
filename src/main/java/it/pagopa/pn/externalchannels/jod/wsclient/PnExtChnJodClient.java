package it.pagopa.pn.externalchannels.jod.wsclient;

import it.pagopa.pn.externalchannels.pojos.JodAuth;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import javax.xml.soap.*;
import java.util.UUID;

public class PnExtChnJodClient extends WebServiceGatewaySupport {

	ObjectFactory factory = new ObjectFactory();

	public IdUploadResponse initUpload(InitUploadRequest request, JodAuth jodAuth) {
		InitUploadRequestWrapper reqWrapper = factory.createInitUploadRequestWrapper();
		reqWrapper.setArg0(request);
		IdUploadResponseWrapper resWrapper = (IdUploadResponseWrapper) getWebServiceTemplate()
				.marshalSendAndReceive(factory.createInitUploadRequestWrapper(reqWrapper), headerProcessor(jodAuth));
		return resWrapper.getReturn();
	}

	public IdPartUploadResponse filePartUpload(FilePartUploadRequest request, JodAuth jodAuth) {
		FilePartUploadRequestWrapper reqWrapper = factory.createFilePartUploadRequestWrapper();
		reqWrapper.setArg0(request);
		UploadResponseWrapper resWrapper = (UploadResponseWrapper) getWebServiceTemplate()
				.marshalSendAndReceive(factory.createFilePartUploadRequestWrapper(reqWrapper), headerProcessor(jodAuth));
		return resWrapper.getReturn();
	}
	
	public EndUploadResponse endFileUpload(EndUploadRequest request, JodAuth jodAuth) {
		EndUploadRequestWrapper reqWrapper = factory.createEndUploadRequestWrapper();
		reqWrapper.setArg0(request);
		EndUploadResponseWrapper resWrapper = (EndUploadResponseWrapper) getWebServiceTemplate()
				.marshalSendAndReceive(factory.createEndUploadRequestWrapper(reqWrapper), headerProcessor(jodAuth));
		return resWrapper.getReturn();
	}
	
	public StatusUploadResponse statusFileUpload(StatusEndUploadRequest request, JodAuth jodAuth) {
		StatusEndUploadRequestWrapper reqWrapper = factory.createStatusEndUploadRequestWrapper();
		reqWrapper.setArg0(request);
		StatusUploadResponseWrapper resWrapper = (StatusUploadResponseWrapper) getWebServiceTemplate()
				.marshalSendAndReceive(factory.createStatusEndUploadRequestWrapper(reqWrapper), headerProcessor(jodAuth));
		return resWrapper.getReturn();
	}

	private WebServiceMessageCallback headerProcessor(JodAuth auth){
		return message -> {
			try {
				SaajSoapMessage saajSoapMessage = (SaajSoapMessage)message;

				SOAPMessage soapMessage = saajSoapMessage.getSaajMessage();

				SOAPPart soapPart = soapMessage.getSOAPPart();

				SOAPEnvelope soapEnvelope = soapPart.getEnvelope();

				SOAPHeader soapHeader = soapEnvelope.getHeader();

				Name headerElementName = soapEnvelope.createName(
						"Security",
						"wsse",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
				);
				SOAPHeaderElement securityElement = soapHeader.addHeaderElement(headerElementName);
				securityElement.addAttribute(soapEnvelope.createName("mustUnderstand"), "true");

				SOAPElement tokenElement = securityElement.addChildElement("UsernameToken", "wsse");
				tokenElement.addAttribute(soapEnvelope.createName("Id"), "uuid_" + UUID.randomUUID());

				SOAPElement usernameElement = tokenElement.addChildElement("Username", "wsse");
				usernameElement.addTextNode(auth.getUsername());

				SOAPElement passwordElement = tokenElement.addChildElement("Password", "wsse");
				passwordElement.addTextNode(auth.getPassword());
				passwordElement.addAttribute(soapEnvelope.createName("Type"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");

				soapMessage.saveChanges();
			} catch (Exception e) {
				// exception handling
			}
		};
	}
}
