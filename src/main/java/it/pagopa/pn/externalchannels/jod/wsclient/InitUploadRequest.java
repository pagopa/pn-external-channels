
package it.pagopa.pn.externalchannels.jod.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per initUploadRequest complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
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
     * Recupera il valore della proprietà chunckSize.
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
     * Imposta il valore della proprietà chunckSize.
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
     * Recupera il valore della proprietà codiceCliente.
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
     * Imposta il valore della proprietà codiceCliente.
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
     * Recupera il valore della proprietà codiceServizio.
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
     * Imposta il valore della proprietà codiceServizio.
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
     * Recupera il valore della proprietà fileName.
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
     * Imposta il valore della proprietà fileName.
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
     * Recupera il valore della proprietà md5SumFile.
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
     * Imposta il valore della proprietà md5SumFile.
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
