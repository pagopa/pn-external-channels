
package it.pagopa.pn.externalchannels.jod.wsclient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the it.pagopa.pn.externalchannels.jod.wsclient package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _EndUploadRequestWrapper_QNAME = new QName("http://jod.upload.postecom.it", "EndUploadRequestWrapper");
    private final static QName _EndUploadResponseWrapper_QNAME = new QName("http://jod.upload.postecom.it", "EndUploadResponseWrapper");
    private final static QName _ErrorDescriptionResponseWrapper_QNAME = new QName("http://jod.upload.postecom.it", "ErrorDescriptionResponseWrapper");
    private final static QName _FilePartUploadRequestWrapper_QNAME = new QName("http://jod.upload.postecom.it", "FilePartUploadRequestWrapper");
    private final static QName _IdUploadResponseWrapper_QNAME = new QName("http://jod.upload.postecom.it", "IdUploadResponseWrapper");
    private final static QName _InitUploadRequestWrapper_QNAME = new QName("http://jod.upload.postecom.it", "InitUploadRequestWrapper");
    private final static QName _MacroServicesResponseWrapper_QNAME = new QName("http://jod.upload.postecom.it", "MacroServicesResponseWrapper");
    private final static QName _StatusEndUploadRequestWrapper_QNAME = new QName("http://jod.upload.postecom.it", "StatusEndUploadRequestWrapper");
    private final static QName _StatusUploadResponseWrapper_QNAME = new QName("http://jod.upload.postecom.it", "StatusUploadResponseWrapper");
    private final static QName _UploadResponseWrapper_QNAME = new QName("http://jod.upload.postecom.it", "UploadResponseWrapper");
    private final static QName _ErrorDescription_QNAME = new QName("http://jod.upload.postecom.it", "errorDescription");
    private final static QName _Macroservices_QNAME = new QName("http://jod.upload.postecom.it", "macroservices");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: it.pagopa.pn.externalchannels.jod.wsclient
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MacroServicesResponse }
     * 
     */
    public MacroServicesResponse createMacroServicesResponse() {
        return new MacroServicesResponse();
    }

    /**
     * Create an instance of {@link MacroServicesResponse.Macroservices }
     * 
     */
    public MacroServicesResponse.Macroservices createMacroServicesResponseMacroservices() {
        return new MacroServicesResponse.Macroservices();
    }

    /**
     * Create an instance of {@link EndUploadRequestWrapper }
     * 
     */
    public EndUploadRequestWrapper createEndUploadRequestWrapper() {
        return new EndUploadRequestWrapper();
    }

    /**
     * Create an instance of {@link EndUploadResponseWrapper }
     * 
     */
    public EndUploadResponseWrapper createEndUploadResponseWrapper() {
        return new EndUploadResponseWrapper();
    }

    /**
     * Create an instance of {@link ErrorDescriptionResponseWrapper }
     * 
     */
    public ErrorDescriptionResponseWrapper createErrorDescriptionResponseWrapper() {
        return new ErrorDescriptionResponseWrapper();
    }

    /**
     * Create an instance of {@link FilePartUploadRequestWrapper }
     * 
     */
    public FilePartUploadRequestWrapper createFilePartUploadRequestWrapper() {
        return new FilePartUploadRequestWrapper();
    }

    /**
     * Create an instance of {@link IdUploadResponseWrapper }
     * 
     */
    public IdUploadResponseWrapper createIdUploadResponseWrapper() {
        return new IdUploadResponseWrapper();
    }

    /**
     * Create an instance of {@link InitUploadRequestWrapper }
     * 
     */
    public InitUploadRequestWrapper createInitUploadRequestWrapper() {
        return new InitUploadRequestWrapper();
    }

    /**
     * Create an instance of {@link MacroServicesResponseWrapper }
     * 
     */
    public MacroServicesResponseWrapper createMacroServicesResponseWrapper() {
        return new MacroServicesResponseWrapper();
    }

    /**
     * Create an instance of {@link StatusEndUploadRequestWrapper }
     * 
     */
    public StatusEndUploadRequestWrapper createStatusEndUploadRequestWrapper() {
        return new StatusEndUploadRequestWrapper();
    }

    /**
     * Create an instance of {@link StatusUploadResponseWrapper }
     * 
     */
    public StatusUploadResponseWrapper createStatusUploadResponseWrapper() {
        return new StatusUploadResponseWrapper();
    }

    /**
     * Create an instance of {@link UploadResponseWrapper }
     * 
     */
    public UploadResponseWrapper createUploadResponseWrapper() {
        return new UploadResponseWrapper();
    }

    /**
     * Create an instance of {@link ErrorDescription }
     * 
     */
    public ErrorDescription createErrorDescription() {
        return new ErrorDescription();
    }

    /**
     * Create an instance of {@link it.pagopa.pn.externalchannels.jod.wsclient.Macroservices }
     * 
     */
    public it.pagopa.pn.externalchannels.jod.wsclient.Macroservices createMacroservices() {
        return new it.pagopa.pn.externalchannels.jod.wsclient.Macroservices();
    }

    /**
     * Create an instance of {@link ErrorDescriptionResponse }
     * 
     */
    public ErrorDescriptionResponse createErrorDescriptionResponse() {
        return new ErrorDescriptionResponse();
    }

    /**
     * Create an instance of {@link InitUploadRequest }
     * 
     */
    public InitUploadRequest createInitUploadRequest() {
        return new InitUploadRequest();
    }

    /**
     * Create an instance of {@link IdUploadResponse }
     * 
     */
    public IdUploadResponse createIdUploadResponse() {
        return new IdUploadResponse();
    }

    /**
     * Create an instance of {@link UploadResponse }
     * 
     */
    public UploadResponse createUploadResponse() {
        return new UploadResponse();
    }

    /**
     * Create an instance of {@link StatusEndUploadRequest }
     * 
     */
    public StatusEndUploadRequest createStatusEndUploadRequest() {
        return new StatusEndUploadRequest();
    }

    /**
     * Create an instance of {@link EndUploadRequest }
     * 
     */
    public EndUploadRequest createEndUploadRequest() {
        return new EndUploadRequest();
    }

    /**
     * Create an instance of {@link StatusUploadResponse }
     * 
     */
    public StatusUploadResponse createStatusUploadResponse() {
        return new StatusUploadResponse();
    }

    /**
     * Create an instance of {@link EndUploadResponse }
     * 
     */
    public EndUploadResponse createEndUploadResponse() {
        return new EndUploadResponse();
    }

    /**
     * Create an instance of {@link FilePartUploadRequest }
     * 
     */
    public FilePartUploadRequest createFilePartUploadRequest() {
        return new FilePartUploadRequest();
    }

    /**
     * Create an instance of {@link IdPartUploadResponse }
     * 
     */
    public IdPartUploadResponse createIdPartUploadResponse() {
        return new IdPartUploadResponse();
    }

    /**
     * Create an instance of {@link MacroServicesResponse.Macroservices.Entry }
     * 
     */
    public MacroServicesResponse.Macroservices.Entry createMacroServicesResponseMacroservicesEntry() {
        return new MacroServicesResponse.Macroservices.Entry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EndUploadRequestWrapper }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EndUploadRequestWrapper }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "EndUploadRequestWrapper")
    public JAXBElement<EndUploadRequestWrapper> createEndUploadRequestWrapper(EndUploadRequestWrapper value) {
        return new JAXBElement<EndUploadRequestWrapper>(_EndUploadRequestWrapper_QNAME, EndUploadRequestWrapper.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EndUploadResponseWrapper }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link EndUploadResponseWrapper }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "EndUploadResponseWrapper")
    public JAXBElement<EndUploadResponseWrapper> createEndUploadResponseWrapper(EndUploadResponseWrapper value) {
        return new JAXBElement<EndUploadResponseWrapper>(_EndUploadResponseWrapper_QNAME, EndUploadResponseWrapper.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ErrorDescriptionResponseWrapper }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ErrorDescriptionResponseWrapper }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "ErrorDescriptionResponseWrapper")
    public JAXBElement<ErrorDescriptionResponseWrapper> createErrorDescriptionResponseWrapper(ErrorDescriptionResponseWrapper value) {
        return new JAXBElement<ErrorDescriptionResponseWrapper>(_ErrorDescriptionResponseWrapper_QNAME, ErrorDescriptionResponseWrapper.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FilePartUploadRequestWrapper }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link FilePartUploadRequestWrapper }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "FilePartUploadRequestWrapper")
    public JAXBElement<FilePartUploadRequestWrapper> createFilePartUploadRequestWrapper(FilePartUploadRequestWrapper value) {
        return new JAXBElement<FilePartUploadRequestWrapper>(_FilePartUploadRequestWrapper_QNAME, FilePartUploadRequestWrapper.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdUploadResponseWrapper }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IdUploadResponseWrapper }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "IdUploadResponseWrapper")
    public JAXBElement<IdUploadResponseWrapper> createIdUploadResponseWrapper(IdUploadResponseWrapper value) {
        return new JAXBElement<IdUploadResponseWrapper>(_IdUploadResponseWrapper_QNAME, IdUploadResponseWrapper.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InitUploadRequestWrapper }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link InitUploadRequestWrapper }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "InitUploadRequestWrapper")
    public JAXBElement<InitUploadRequestWrapper> createInitUploadRequestWrapper(InitUploadRequestWrapper value) {
        return new JAXBElement<InitUploadRequestWrapper>(_InitUploadRequestWrapper_QNAME, InitUploadRequestWrapper.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MacroServicesResponseWrapper }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link MacroServicesResponseWrapper }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "MacroServicesResponseWrapper")
    public JAXBElement<MacroServicesResponseWrapper> createMacroServicesResponseWrapper(MacroServicesResponseWrapper value) {
        return new JAXBElement<MacroServicesResponseWrapper>(_MacroServicesResponseWrapper_QNAME, MacroServicesResponseWrapper.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatusEndUploadRequestWrapper }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StatusEndUploadRequestWrapper }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "StatusEndUploadRequestWrapper")
    public JAXBElement<StatusEndUploadRequestWrapper> createStatusEndUploadRequestWrapper(StatusEndUploadRequestWrapper value) {
        return new JAXBElement<StatusEndUploadRequestWrapper>(_StatusEndUploadRequestWrapper_QNAME, StatusEndUploadRequestWrapper.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatusUploadResponseWrapper }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StatusUploadResponseWrapper }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "StatusUploadResponseWrapper")
    public JAXBElement<StatusUploadResponseWrapper> createStatusUploadResponseWrapper(StatusUploadResponseWrapper value) {
        return new JAXBElement<StatusUploadResponseWrapper>(_StatusUploadResponseWrapper_QNAME, StatusUploadResponseWrapper.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadResponseWrapper }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link UploadResponseWrapper }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "UploadResponseWrapper")
    public JAXBElement<UploadResponseWrapper> createUploadResponseWrapper(UploadResponseWrapper value) {
        return new JAXBElement<UploadResponseWrapper>(_UploadResponseWrapper_QNAME, UploadResponseWrapper.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ErrorDescription }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ErrorDescription }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "errorDescription")
    public JAXBElement<ErrorDescription> createErrorDescription(ErrorDescription value) {
        return new JAXBElement<ErrorDescription>(_ErrorDescription_QNAME, ErrorDescription.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link it.pagopa.pn.externalchannels.jod.wsclient.Macroservices }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link it.pagopa.pn.externalchannels.jod.wsclient.Macroservices }{@code >}
     */
    @XmlElementDecl(namespace = "http://jod.upload.postecom.it", name = "macroservices")
    public JAXBElement<it.pagopa.pn.externalchannels.jod.wsclient.Macroservices> createMacroservices(it.pagopa.pn.externalchannels.jod.wsclient.Macroservices value) {
        return new JAXBElement<it.pagopa.pn.externalchannels.jod.wsclient.Macroservices>(_Macroservices_QNAME, it.pagopa.pn.externalchannels.jod.wsclient.Macroservices.class, null, value);
    }

}
