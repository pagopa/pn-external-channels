package it.pagopa.pn.externalchannels.dto.postelmock;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * MasterOut
 */
@JsonPropertyOrder({
        MasterOut.JSON_PROPERTY_ID,
        MasterOut.JSON_PROPERTY_N_RISULTATO_NORM,
        MasterOut.JSON_PROPERTY_N_ERRORE_NORM,
        MasterOut.JSON_PROPERTY_S_SIGLA_PROV,
        MasterOut.JSON_PROPERTY_F_POSTALIZZABILE,
        MasterOut.JSON_PROPERTY_S_STATO_UFF,
        MasterOut.JSON_PROPERTY_S_STATO_ABB,
        MasterOut.JSON_PROPERTY_S_STATO_SPEDIZIONE,
        MasterOut.JSON_PROPERTY_S_COMUNE_UFF,
        MasterOut.JSON_PROPERTY_S_COMUNE_ABB,
        MasterOut.JSON_PROPERTY_S_COMUNE_SPEDIZIONE,
        MasterOut.JSON_PROPERTY_S_FRAZIONE_UFF,
        MasterOut.JSON_PROPERTY_S_FRAZIONE_ABB,
        MasterOut.JSON_PROPERTY_S_FRAZIONE_SPEDIZIONE,
        MasterOut.JSON_PROPERTY_S_CIVICO_ALTRO,
        MasterOut.JSON_PROPERTY_S_CAP,
        MasterOut.JSON_PROPERTY_S_PRESSO,
        MasterOut.JSON_PROPERTY_S_VIA_COMPLETA_UFF,
        MasterOut.JSON_PROPERTY_S_VIA_COMPLETA_ABB,
        MasterOut.JSON_PROPERTY_S_VIA_COMPLETA_SPEDIZIONE
})
@JsonTypeName("MasterOut")
public class MasterOut {
    public static final String JSON_PROPERTY_ID = "id";
    private String id;

    public static final String JSON_PROPERTY_N_RISULTATO_NORM = "nRisultatoNorm";
    private Integer nRisultatoNorm;

    public static final String JSON_PROPERTY_N_ERRORE_NORM = "nErroreNorm";
    private Integer nErroreNorm;

    public static final String JSON_PROPERTY_S_SIGLA_PROV = "sSiglaProv";
    private String sSiglaProv;

    public static final String JSON_PROPERTY_F_POSTALIZZABILE = "fPostalizzabile";
    private String fPostalizzabile;

    public static final String JSON_PROPERTY_S_STATO_UFF = "sStatoUff";
    private String sStatoUff;

    public static final String JSON_PROPERTY_S_STATO_ABB = "sStatoAbb";
    private String sStatoAbb;

    public static final String JSON_PROPERTY_S_STATO_SPEDIZIONE = "sStatoSpedizione";
    private String sStatoSpedizione;

    public static final String JSON_PROPERTY_S_COMUNE_UFF = "sComuneUff";
    private String sComuneUff;

    public static final String JSON_PROPERTY_S_COMUNE_ABB = "sComuneAbb";
    private String sComuneAbb;

    public static final String JSON_PROPERTY_S_COMUNE_SPEDIZIONE = "sComuneSpedizione";
    private String sComuneSpedizione;

    public static final String JSON_PROPERTY_S_FRAZIONE_UFF = "sFrazioneUff";
    private String sFrazioneUff;

    public static final String JSON_PROPERTY_S_FRAZIONE_ABB = "sFrazioneAbb";
    private String sFrazioneAbb;

    public static final String JSON_PROPERTY_S_FRAZIONE_SPEDIZIONE = "sFrazioneSpedizione";
    private String sFrazioneSpedizione;

    public static final String JSON_PROPERTY_S_CIVICO_ALTRO = "sCivicoAltro";
    private String sCivicoAltro;

    public static final String JSON_PROPERTY_S_CAP = "sCap";
    private String sCap;

    public static final String JSON_PROPERTY_S_PRESSO = "sPresso";
    private String sPresso;

    public static final String JSON_PROPERTY_S_VIA_COMPLETA_UFF = "sViaCompletaUff";
    private String sViaCompletaUff;

    public static final String JSON_PROPERTY_S_VIA_COMPLETA_ABB = "sViaCompletaAbb";
    private String sViaCompletaAbb;

    public static final String JSON_PROPERTY_S_VIA_COMPLETA_SPEDIZIONE = "sViaCompletaSpedizione";
    private String sViaCompletaSpedizione;

    public MasterOut() {
        // TODO document why this constructor is empty
    }

    public MasterOut id(String id) {

        this.id = id;
        return this;
    }

    /**
     * Get id
     * @return id
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getId() {
        return id;
    }


    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(String id) {
        this.id = id;
    }


    public MasterOut nRisultatoNorm(Integer nRisultatoNorm) {

        this.nRisultatoNorm = nRisultatoNorm;
        return this;
    }

    /**
     * Get nRisultatoNorm
     * @return nRisultatoNorm
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_N_RISULTATO_NORM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getnRisultatoNorm() {
        return nRisultatoNorm;
    }


    @JsonProperty(JSON_PROPERTY_N_RISULTATO_NORM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setnRisultatoNorm(Integer nRisultatoNorm) {
        this.nRisultatoNorm = nRisultatoNorm;
    }


    public MasterOut nErroreNorm(Integer nErroreNorm) {

        this.nErroreNorm = nErroreNorm;
        return this;
    }

    /**
     * Get nErroreNorm
     * @return nErroreNorm
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_N_ERRORE_NORM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getnErroreNorm() {
        return nErroreNorm;
    }


    @JsonProperty(JSON_PROPERTY_N_ERRORE_NORM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setnErroreNorm(Integer nErroreNorm) {
        this.nErroreNorm = nErroreNorm;
    }


    public MasterOut sSiglaProv(String sSiglaProv) {

        this.sSiglaProv = sSiglaProv;
        return this;
    }

    /**
     * Get sSiglaProv
     * @return sSiglaProv
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_SIGLA_PROV)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsSiglaProv() {
        return sSiglaProv;
    }


    @JsonProperty(JSON_PROPERTY_S_SIGLA_PROV)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsSiglaProv(String sSiglaProv) {
        this.sSiglaProv = sSiglaProv;
    }


    public MasterOut fPostalizzabile(String fPostalizzabile) {

        this.fPostalizzabile = fPostalizzabile;
        return this;
    }

    /**
     * Get fPostalizzabile
     * @return fPostalizzabile
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_F_POSTALIZZABILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getfPostalizzabile() {
        return fPostalizzabile;
    }


    @JsonProperty(JSON_PROPERTY_F_POSTALIZZABILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setfPostalizzabile(String fPostalizzabile) {
        this.fPostalizzabile = fPostalizzabile;
    }


    public MasterOut sStatoUff(String sStatoUff) {

        this.sStatoUff = sStatoUff;
        return this;
    }

    /**
     * Get sStatoUff
     * @return sStatoUff
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_STATO_UFF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsStatoUff() {
        return sStatoUff;
    }


    @JsonProperty(JSON_PROPERTY_S_STATO_UFF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsStatoUff(String sStatoUff) {
        this.sStatoUff = sStatoUff;
    }


    public MasterOut sStatoAbb(String sStatoAbb) {

        this.sStatoAbb = sStatoAbb;
        return this;
    }

    /**
     * Get sStatoAbb
     * @return sStatoAbb
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_STATO_ABB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsStatoAbb() {
        return sStatoAbb;
    }


    @JsonProperty(JSON_PROPERTY_S_STATO_ABB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsStatoAbb(String sStatoAbb) {
        this.sStatoAbb = sStatoAbb;
    }


    public MasterOut sStatoSpedizione(String sStatoSpedizione) {

        this.sStatoSpedizione = sStatoSpedizione;
        return this;
    }

    /**
     * Get sStatoSpedizione
     * @return sStatoSpedizione
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_STATO_SPEDIZIONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsStatoSpedizione() {
        return sStatoSpedizione;
    }


    @JsonProperty(JSON_PROPERTY_S_STATO_SPEDIZIONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsStatoSpedizione(String sStatoSpedizione) {
        this.sStatoSpedizione = sStatoSpedizione;
    }


    public MasterOut sComuneUff(String sComuneUff) {

        this.sComuneUff = sComuneUff;
        return this;
    }

    /**
     * Get sComuneUff
     * @return sComuneUff
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_COMUNE_UFF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsComuneUff() {
        return sComuneUff;
    }


    @JsonProperty(JSON_PROPERTY_S_COMUNE_UFF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsComuneUff(String sComuneUff) {
        this.sComuneUff = sComuneUff;
    }


    public MasterOut sComuneAbb(String sComuneAbb) {

        this.sComuneAbb = sComuneAbb;
        return this;
    }

    /**
     * Get sComuneAbb
     * @return sComuneAbb
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_COMUNE_ABB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsComuneAbb() {
        return sComuneAbb;
    }


    @JsonProperty(JSON_PROPERTY_S_COMUNE_ABB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsComuneAbb(String sComuneAbb) {
        this.sComuneAbb = sComuneAbb;
    }


    public MasterOut sComuneSpedizione(String sComuneSpedizione) {

        this.sComuneSpedizione = sComuneSpedizione;
        return this;
    }

    /**
     * Get sComuneSpedizione
     * @return sComuneSpedizione
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_COMUNE_SPEDIZIONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsComuneSpedizione() {
        return sComuneSpedizione;
    }


    @JsonProperty(JSON_PROPERTY_S_COMUNE_SPEDIZIONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsComuneSpedizione(String sComuneSpedizione) {
        this.sComuneSpedizione = sComuneSpedizione;
    }


    public MasterOut sFrazioneUff(String sFrazioneUff) {

        this.sFrazioneUff = sFrazioneUff;
        return this;
    }

    /**
     * Get sFrazioneUff
     * @return sFrazioneUff
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_FRAZIONE_UFF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsFrazioneUff() {
        return sFrazioneUff;
    }


    @JsonProperty(JSON_PROPERTY_S_FRAZIONE_UFF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsFrazioneUff(String sFrazioneUff) {
        this.sFrazioneUff = sFrazioneUff;
    }


    public MasterOut sFrazioneAbb(String sFrazioneAbb) {

        this.sFrazioneAbb = sFrazioneAbb;
        return this;
    }

    /**
     * Get sFrazioneAbb
     * @return sFrazioneAbb
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_FRAZIONE_ABB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsFrazioneAbb() {
        return sFrazioneAbb;
    }


    @JsonProperty(JSON_PROPERTY_S_FRAZIONE_ABB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsFrazioneAbb(String sFrazioneAbb) {
        this.sFrazioneAbb = sFrazioneAbb;
    }


    public MasterOut sFrazioneSpedizione(String sFrazioneSpedizione) {

        this.sFrazioneSpedizione = sFrazioneSpedizione;
        return this;
    }

    /**
     * Get sFrazioneSpedizione
     * @return sFrazioneSpedizione
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_FRAZIONE_SPEDIZIONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsFrazioneSpedizione() {
        return sFrazioneSpedizione;
    }


    @JsonProperty(JSON_PROPERTY_S_FRAZIONE_SPEDIZIONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsFrazioneSpedizione(String sFrazioneSpedizione) {
        this.sFrazioneSpedizione = sFrazioneSpedizione;
    }


    public MasterOut sCivicoAltro(String sCivicoAltro) {

        this.sCivicoAltro = sCivicoAltro;
        return this;
    }

    /**
     * Get sCivicoAltro
     * @return sCivicoAltro
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_CIVICO_ALTRO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsCivicoAltro() {
        return sCivicoAltro;
    }


    @JsonProperty(JSON_PROPERTY_S_CIVICO_ALTRO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsCivicoAltro(String sCivicoAltro) {
        this.sCivicoAltro = sCivicoAltro;
    }


    public MasterOut sCap(String sCap) {

        this.sCap = sCap;
        return this;
    }

    /**
     * Get sCap
     * @return sCap
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_CAP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsCap() {
        return sCap;
    }


    @JsonProperty(JSON_PROPERTY_S_CAP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsCap(String sCap) {
        this.sCap = sCap;
    }


    public MasterOut sPresso(String sPresso) {

        this.sPresso = sPresso;
        return this;
    }

    /**
     * Get sPresso
     * @return sPresso
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_PRESSO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsPresso() {
        return sPresso;
    }


    @JsonProperty(JSON_PROPERTY_S_PRESSO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsPresso(String sPresso) {
        this.sPresso = sPresso;
    }


    public MasterOut sViaCompletaUff(String sViaCompletaUff) {

        this.sViaCompletaUff = sViaCompletaUff;
        return this;
    }

    /**
     * Get sViaCompletaUff
     * @return sViaCompletaUff
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_VIA_COMPLETA_UFF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsViaCompletaUff() {
        return sViaCompletaUff;
    }


    @JsonProperty(JSON_PROPERTY_S_VIA_COMPLETA_UFF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsViaCompletaUff(String sViaCompletaUff) {
        this.sViaCompletaUff = sViaCompletaUff;
    }


    public MasterOut sViaCompletaAbb(String sViaCompletaAbb) {

        this.sViaCompletaAbb = sViaCompletaAbb;
        return this;
    }

    /**
     * Get sViaCompletaAbb
     * @return sViaCompletaAbb
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_VIA_COMPLETA_ABB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsViaCompletaAbb() {
        return sViaCompletaAbb;
    }


    @JsonProperty(JSON_PROPERTY_S_VIA_COMPLETA_ABB)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsViaCompletaAbb(String sViaCompletaAbb) {
        this.sViaCompletaAbb = sViaCompletaAbb;
    }


    public MasterOut sViaCompletaSpedizione(String sViaCompletaSpedizione) {

        this.sViaCompletaSpedizione = sViaCompletaSpedizione;
        return this;
    }

    /**
     * Get sViaCompletaSpedizione
     * @return sViaCompletaSpedizione
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_VIA_COMPLETA_SPEDIZIONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsViaCompletaSpedizione() {
        return sViaCompletaSpedizione;
    }


    @JsonProperty(JSON_PROPERTY_S_VIA_COMPLETA_SPEDIZIONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsViaCompletaSpedizione(String sViaCompletaSpedizione) {
        this.sViaCompletaSpedizione = sViaCompletaSpedizione;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MasterOut masterOut = (MasterOut) o;
        return Objects.equals(this.id, masterOut.id) &&
                Objects.equals(this.nRisultatoNorm, masterOut.nRisultatoNorm) &&
                Objects.equals(this.nErroreNorm, masterOut.nErroreNorm) &&
                Objects.equals(this.sSiglaProv, masterOut.sSiglaProv) &&
                Objects.equals(this.fPostalizzabile, masterOut.fPostalizzabile) &&
                Objects.equals(this.sStatoUff, masterOut.sStatoUff) &&
                Objects.equals(this.sStatoAbb, masterOut.sStatoAbb) &&
                Objects.equals(this.sStatoSpedizione, masterOut.sStatoSpedizione) &&
                Objects.equals(this.sComuneUff, masterOut.sComuneUff) &&
                Objects.equals(this.sComuneAbb, masterOut.sComuneAbb) &&
                Objects.equals(this.sComuneSpedizione, masterOut.sComuneSpedizione) &&
                Objects.equals(this.sFrazioneUff, masterOut.sFrazioneUff) &&
                Objects.equals(this.sFrazioneAbb, masterOut.sFrazioneAbb) &&
                Objects.equals(this.sFrazioneSpedizione, masterOut.sFrazioneSpedizione) &&
                Objects.equals(this.sCivicoAltro, masterOut.sCivicoAltro) &&
                Objects.equals(this.sCap, masterOut.sCap) &&
                Objects.equals(this.sPresso, masterOut.sPresso) &&
                Objects.equals(this.sViaCompletaUff, masterOut.sViaCompletaUff) &&
                Objects.equals(this.sViaCompletaAbb, masterOut.sViaCompletaAbb) &&
                Objects.equals(this.sViaCompletaSpedizione, masterOut.sViaCompletaSpedizione);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nRisultatoNorm, nErroreNorm, sSiglaProv, fPostalizzabile, sStatoUff, sStatoAbb, sStatoSpedizione, sComuneUff, sComuneAbb, sComuneSpedizione, sFrazioneUff, sFrazioneAbb, sFrazioneSpedizione, sCivicoAltro, sCap, sPresso, sViaCompletaUff, sViaCompletaAbb, sViaCompletaSpedizione);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MasterOut {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    nRisultatoNorm: ").append(toIndentedString(nRisultatoNorm)).append("\n");
        sb.append("    nErroreNorm: ").append(toIndentedString(nErroreNorm)).append("\n");
        sb.append("    sSiglaProv: ").append(toIndentedString(sSiglaProv)).append("\n");
        sb.append("    fPostalizzabile: ").append(toIndentedString(fPostalizzabile)).append("\n");
        sb.append("    sStatoUff: ").append(toIndentedString(sStatoUff)).append("\n");
        sb.append("    sStatoAbb: ").append(toIndentedString(sStatoAbb)).append("\n");
        sb.append("    sStatoSpedizione: ").append(toIndentedString(sStatoSpedizione)).append("\n");
        sb.append("    sComuneUff: ").append(toIndentedString(sComuneUff)).append("\n");
        sb.append("    sComuneAbb: ").append(toIndentedString(sComuneAbb)).append("\n");
        sb.append("    sComuneSpedizione: ").append(toIndentedString(sComuneSpedizione)).append("\n");
        sb.append("    sFrazioneUff: ").append(toIndentedString(sFrazioneUff)).append("\n");
        sb.append("    sFrazioneAbb: ").append(toIndentedString(sFrazioneAbb)).append("\n");
        sb.append("    sFrazioneSpedizione: ").append(toIndentedString(sFrazioneSpedizione)).append("\n");
        sb.append("    sCivicoAltro: ").append(toIndentedString(sCivicoAltro)).append("\n");
        sb.append("    sCap: ").append(toIndentedString(sCap)).append("\n");
        sb.append("    sPresso: ").append(toIndentedString(sPresso)).append("\n");
        sb.append("    sViaCompletaUff: ").append(toIndentedString(sViaCompletaUff)).append("\n");
        sb.append("    sViaCompletaAbb: ").append(toIndentedString(sViaCompletaAbb)).append("\n");
        sb.append("    sViaCompletaSpedizione: ").append(toIndentedString(sViaCompletaSpedizione)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

