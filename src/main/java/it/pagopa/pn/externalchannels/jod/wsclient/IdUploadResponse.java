
package it.pagopa.pn.externalchannels.jod.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for idUploadResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="idUploadResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://jod.upload.postecom.it}uploadResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="uploadstatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="uuid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "idUploadResponse", propOrder = {
    "uploadstatus",
    "uuid"
})
@XmlSeeAlso({
    StatusUploadResponse.class,
    EndUploadResponse.class,
    IdPartUploadResponse.class
})
public class IdUploadResponse
    extends UploadResponse
{

    protected String uploadstatus;
    protected String uuid;

    /**
     * Gets the value of the uploadstatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUploadstatus() {
        return uploadstatus;
    }

    /**
     * Sets the value of the uploadstatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUploadstatus(String value) {
        this.uploadstatus = value;
    }

    /**
     * Gets the value of the uuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

}
