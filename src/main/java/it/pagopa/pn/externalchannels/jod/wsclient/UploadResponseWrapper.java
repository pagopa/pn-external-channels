
package it.pagopa.pn.externalchannels.jod.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per UploadResponseWrapper complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="UploadResponseWrapper"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="return" type="{http://jod.upload.postecom.it}idPartUploadResponse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UploadResponseWrapper", propOrder = {
    "_return"
})
public class UploadResponseWrapper {

    @XmlElement(name = "return")
    protected IdPartUploadResponse _return;

    /**
     * Recupera il valore della proprietà return.
     * 
     * @return
     *     possible object is
     *     {@link IdPartUploadResponse }
     *     
     */
    public IdPartUploadResponse getReturn() {
        return _return;
    }

    /**
     * Imposta il valore della proprietà return.
     * 
     * @param value
     *     allowed object is
     *     {@link IdPartUploadResponse }
     *     
     */
    public void setReturn(IdPartUploadResponse value) {
        this._return = value;
    }

}
