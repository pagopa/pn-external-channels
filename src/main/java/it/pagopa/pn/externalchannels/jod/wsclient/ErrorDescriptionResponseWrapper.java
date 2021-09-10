
package it.pagopa.pn.externalchannels.jod.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per ErrorDescriptionResponseWrapper complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="ErrorDescriptionResponseWrapper"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="return" type="{http://jod.upload.postecom.it}errorDescriptionResponse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ErrorDescriptionResponseWrapper", propOrder = {
    "_return"
})
public class ErrorDescriptionResponseWrapper {

    @XmlElement(name = "return")
    protected ErrorDescriptionResponse _return;

    /**
     * Recupera il valore della proprietà return.
     * 
     * @return
     *     possible object is
     *     {@link ErrorDescriptionResponse }
     *     
     */
    public ErrorDescriptionResponse getReturn() {
        return _return;
    }

    /**
     * Imposta il valore della proprietà return.
     * 
     * @param value
     *     allowed object is
     *     {@link ErrorDescriptionResponse }
     *     
     */
    public void setReturn(ErrorDescriptionResponse value) {
        this._return = value;
    }

}
