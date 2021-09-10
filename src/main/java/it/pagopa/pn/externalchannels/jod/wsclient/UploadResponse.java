
package it.pagopa.pn.externalchannels.jod.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per uploadResponse complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="uploadResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="descriptiveReason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="executiontime" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="reason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uploadResponse", propOrder = {
    "descriptiveReason",
    "executiontime",
    "reason",
    "status"
})
@XmlSeeAlso({
    MacroServicesResponse.class,
    IdUploadResponse.class
})
public class UploadResponse {

    protected String descriptiveReason;
    protected long executiontime;
    protected String reason;
    protected String status;

    /**
     * Recupera il valore della proprietà descriptiveReason.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescriptiveReason() {
        return descriptiveReason;
    }

    /**
     * Imposta il valore della proprietà descriptiveReason.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescriptiveReason(String value) {
        this.descriptiveReason = value;
    }

    /**
     * Recupera il valore della proprietà executiontime.
     * 
     */
    public long getExecutiontime() {
        return executiontime;
    }

    /**
     * Imposta il valore della proprietà executiontime.
     * 
     */
    public void setExecutiontime(long value) {
        this.executiontime = value;
    }

    /**
     * Recupera il valore della proprietà reason.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReason() {
        return reason;
    }

    /**
     * Imposta il valore della proprietà reason.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReason(String value) {
        this.reason = value;
    }

    /**
     * Recupera il valore della proprietà status.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Imposta il valore della proprietà status.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

}
