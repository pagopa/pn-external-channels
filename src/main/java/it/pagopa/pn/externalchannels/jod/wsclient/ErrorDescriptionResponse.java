
package it.pagopa.pn.externalchannels.jod.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per errorDescriptionResponse complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="errorDescriptionResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="errors" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "errorDescriptionResponse", propOrder = {
    "errors"
})
public class ErrorDescriptionResponse {

    protected String errors;

    /**
     * Recupera il valore della proprietà errors.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrors() {
        return errors;
    }

    /**
     * Imposta il valore della proprietà errors.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrors(String value) {
        this.errors = value;
    }

}
