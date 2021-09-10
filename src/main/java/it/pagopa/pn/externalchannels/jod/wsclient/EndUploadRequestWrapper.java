
package it.pagopa.pn.externalchannels.jod.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per EndUploadRequestWrapper complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="EndUploadRequestWrapper"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="arg0" type="{http://jod.upload.postecom.it}endUploadRequest" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EndUploadRequestWrapper", propOrder = {
    "arg0"
})
public class EndUploadRequestWrapper {

    protected EndUploadRequest arg0;

    /**
     * Recupera il valore della proprietà arg0.
     * 
     * @return
     *     possible object is
     *     {@link EndUploadRequest }
     *     
     */
    public EndUploadRequest getArg0() {
        return arg0;
    }

    /**
     * Imposta il valore della proprietà arg0.
     * 
     * @param value
     *     allowed object is
     *     {@link EndUploadRequest }
     *     
     */
    public void setArg0(EndUploadRequest value) {
        this.arg0 = value;
    }

}
