
package it.pagopa.pn.externalchannels.jod.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for initUploadRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="initUploadRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="chunckSize" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="codiceCliente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="codiceServizio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="fileName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="md5SumFile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "initUploadRequest", propOrder = {
    "chunckSize",
    "codiceCliente",
    "codiceServizio",
    "fileName",
    "md5SumFile"
})
public class InitUploadRequest {

    protected String chunckSize;
    protected String codiceCliente;
    protected String codiceServizio;
    protected String fileName;
    protected String md5SumFile;

    /**
     * Gets the value of the chunckSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChunckSize() {
        return chunckSize;
    }

    /**
     * Sets the value of the chunckSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChunckSize(String value) {
        this.chunckSize = value;
    }

    /**
     * Gets the value of the codiceCliente property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodiceCliente() {
        return codiceCliente;
    }

    /**
     * Sets the value of the codiceCliente property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodiceCliente(String value) {
        this.codiceCliente = value;
    }

    /**
     * Gets the value of the codiceServizio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodiceServizio() {
        return codiceServizio;
    }

    /**
     * Sets the value of the codiceServizio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodiceServizio(String value) {
        this.codiceServizio = value;
    }

    /**
     * Gets the value of the fileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the value of the fileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileName(String value) {
        this.fileName = value;
    }

    /**
     * Gets the value of the md5SumFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMd5SumFile() {
        return md5SumFile;
    }

    /**
     * Sets the value of the md5SumFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMd5SumFile(String value) {
        this.md5SumFile = value;
    }

}
