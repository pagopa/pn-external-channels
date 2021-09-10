
package it.pagopa.pn.externalchannels.jod.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for filePartUploadRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="filePartUploadRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://jod.upload.postecom.it}endUploadRequest"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="filePartAsBase64" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="filePartIndex" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="md5SumFilePartAsBase64" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "filePartUploadRequest", propOrder = {
    "filePartAsBase64",
    "filePartIndex",
    "md5SumFilePartAsBase64"
})
public class FilePartUploadRequest
    extends EndUploadRequest
{

    protected String filePartAsBase64;
    protected int filePartIndex;
    protected String md5SumFilePartAsBase64;

    /**
     * Gets the value of the filePartAsBase64 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilePartAsBase64() {
        return filePartAsBase64;
    }

    /**
     * Sets the value of the filePartAsBase64 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilePartAsBase64(String value) {
        this.filePartAsBase64 = value;
    }

    /**
     * Gets the value of the filePartIndex property.
     * 
     */
    public int getFilePartIndex() {
        return filePartIndex;
    }

    /**
     * Sets the value of the filePartIndex property.
     * 
     */
    public void setFilePartIndex(int value) {
        this.filePartIndex = value;
    }

    /**
     * Gets the value of the md5SumFilePartAsBase64 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMd5SumFilePartAsBase64() {
        return md5SumFilePartAsBase64;
    }

    /**
     * Sets the value of the md5SumFilePartAsBase64 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMd5SumFilePartAsBase64(String value) {
        this.md5SumFilePartAsBase64 = value;
    }

}
