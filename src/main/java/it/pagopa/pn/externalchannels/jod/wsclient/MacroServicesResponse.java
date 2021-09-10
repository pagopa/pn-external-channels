
package it.pagopa.pn.externalchannels.jod.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per macroServicesResponse complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="macroServicesResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://jod.upload.postecom.it}uploadResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="codiceCliente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="macroservices"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="entry" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *                             &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="nomeCliente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "macroServicesResponse", propOrder = {
    "codiceCliente",
    "macroservices",
    "nomeCliente"
})
public class MacroServicesResponse
    extends UploadResponse
{

    protected String codiceCliente;
    @XmlElement(required = true)
    protected MacroServicesResponse.Macroservices macroservices;
    protected String nomeCliente;

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
     * Recupera il valore della proprietà macroservices.
     * 
     * @return
     *     possible object is
     *     {@link MacroServicesResponse.Macroservices }
     *     
     */
    public MacroServicesResponse.Macroservices getMacroservices() {
        return macroservices;
    }

    /**
     * Imposta il valore della proprietà macroservices.
     * 
     * @param value
     *     allowed object is
     *     {@link MacroServicesResponse.Macroservices }
     *     
     */
    public void setMacroservices(MacroServicesResponse.Macroservices value) {
        this.macroservices = value;
    }

    /**
     * Recupera il valore della proprietà nomeCliente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNomeCliente() {
        return nomeCliente;
    }

    /**
     * Imposta il valore della proprietà nomeCliente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNomeCliente(String value) {
        this.nomeCliente = value;
    }


    /**
     * <p>Classe Java per anonymous complex type.
     * 
     * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="entry" maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
     *                   &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "entry"
    })
    public static class Macroservices {

        protected List<MacroServicesResponse.Macroservices.Entry> entry;

        /**
         * Gets the value of the entry property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the entry property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEntry().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MacroServicesResponse.Macroservices.Entry }
         * 
         * 
         */
        public List<MacroServicesResponse.Macroservices.Entry> getEntry() {
            if (entry == null) {
                entry = new ArrayList<MacroServicesResponse.Macroservices.Entry>();
            }
            return this.entry;
        }


        /**
         * <p>Classe Java per anonymous complex type.
         * 
         * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
         *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "key",
            "value"
        })
        public static class Entry {

            protected Object key;
            protected Object value;

            /**
             * Recupera il valore della proprietà key.
             * 
             * @return
             *     possible object is
             *     {@link Object }
             *     
             */
            public Object getKey() {
                return key;
            }

            /**
             * Imposta il valore della proprietà key.
             * 
             * @param value
             *     allowed object is
             *     {@link Object }
             *     
             */
            public void setKey(Object value) {
                this.key = value;
            }

            /**
             * Recupera il valore della proprietà value.
             * 
             * @return
             *     possible object is
             *     {@link Object }
             *     
             */
            public Object getValue() {
                return value;
            }

            /**
             * Imposta il valore della proprietà value.
             * 
             * @param value
             *     allowed object is
             *     {@link Object }
             *     
             */
            public void setValue(Object value) {
                this.value = value;
            }

        }

    }

}
