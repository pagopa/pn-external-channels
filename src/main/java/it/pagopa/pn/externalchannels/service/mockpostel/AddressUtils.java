package it.pagopa.pn.externalchannels.service.mockpostel;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.externalchannels.dao.CapModel;
import it.pagopa.pn.externalchannels.dto.postelmock.NormalizeRequestPostelInput;
import it.pagopa.pn.externalchannels.mock_postel.AddressIn;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
@CustomLog
public class AddressUtils {
    private final List<CapModel> capList;
    private final Map<String, String> countryMap;

    public AddressUtils(CsvService csvService) {
        this.capList = csvService.capList();
        this.countryMap = csvService.countryMap();
    }

    public boolean compareAddress(AddressIn baseAddress, AddressIn targetAddress, boolean isItalian) {
        return compare(baseAddress.getIndirizzo(), targetAddress.getIndirizzo())
                && compare(baseAddress.getIndirizzoAggiuntivo(), targetAddress.getIndirizzoAggiuntivo())
                && compare(baseAddress.getCap(), targetAddress.getCap())
                && compare(baseAddress.getLocalita(), targetAddress.getLocalita())
                && compare(baseAddress.getLocalitaAggiuntiva(), targetAddress.getLocalitaAggiuntiva())
                && compare(baseAddress.getProvincia(), targetAddress.getProvincia())
                && (isItalian || compare(baseAddress.getStato(), targetAddress.getStato()));
    }

    private boolean compare(String base, String target) {
        String trimmedBase = StringUtils.normalizeSpace(Optional.ofNullable(base).orElse(""));
        String trimmedTarget = StringUtils.normalizeSpace(Optional.ofNullable(target).orElse(""));
        return trimmedBase.equalsIgnoreCase(trimmedTarget);
    }

    public NormalizeRequestPostelInput normalizeAddress(NormalizeRequestPostelInput address) {
        NormalizeRequestPostelInput normalizedAddress = new NormalizeRequestPostelInput();
        normalizedAddress.setIndirizzo(Optional.ofNullable(address.getIndirizzo()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setLocalita(Optional.ofNullable(address.getLocalita()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setCap(Optional.ofNullable(address.getCap()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setProvincia(Optional.ofNullable(address.getProvincia()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setIndirizzoAggiuntivo(Optional.ofNullable(address.getIndirizzoAggiuntivo()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setLocalitaAggiuntiva(Optional.ofNullable(address.getLocalitaAggiuntiva()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setStato(Optional.ofNullable(address.getStato()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        return normalizedAddress;
    }

    public AddressIn normalizeAddress(AddressIn address) {
        AddressIn normalizedAddress = new AddressIn();
        normalizedAddress.setIndirizzo(Optional.ofNullable(address.getIndirizzo()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setLocalita(Optional.ofNullable(address.getLocalita()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setCap(Optional.ofNullable(address.getCap()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setProvincia(Optional.ofNullable(address.getProvincia()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setIndirizzoAggiuntivo(Optional.ofNullable(address.getIndirizzoAggiuntivo()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setLocalitaAggiuntiva(Optional.ofNullable(address.getLocalitaAggiuntiva()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        normalizedAddress.setStato(Optional.ofNullable(address.getStato()).map(s -> StringUtils.normalizeSpace(s).toUpperCase()).orElse(null));
        return normalizedAddress;
    }

    public boolean isItalian(String stato) {
        return StringUtils.isBlank(stato) || stato.toUpperCase().trim().startsWith("ITAL");
    }


    public void verifyCapAndCity(String cap, String province, String city) {
        if (StringUtils.isBlank(cap)
                || StringUtils.isBlank(city)
                || StringUtils.isBlank(province)) {
            throw new PnInternalException("Cap, city and Province are mandatory", "CAP_NOT_FOUND");
        } else if (!compareWithCapModelObject(cap, province, city)) {
            throw new PnInternalException("Invalid Address, Cap, City and Province: [" + cap + "," + city + "," + province + "]", "CAP_NOT_FOUND");
        }
    }

    private boolean compareWithCapModelObject(String cap, String province, String city) {
        return capList.stream()
                .anyMatch(capModel -> capModel.getCap().equalsIgnoreCase(cap.trim())
                        && capModel.getProvince().equalsIgnoreCase(province.trim())
                        && compareCity(capModel.getCity(), city));
    }

    public void searchCountry(String country) {
        String normalizedCountry = StringUtils.normalizeSpace(StringUtils.upperCase(country));
        if (!countryMap.containsKey(normalizedCountry)) {
            throw new PnInternalException(String.format("Country not found: [%s]", normalizedCountry),"COUNTRY_NOT_FOUND");
        }
    }

    public boolean compareCity(String cityCSV, String cityInput) {
        if(StringUtils.isNoneBlank(cityCSV) && StringUtils.isNoneBlank(cityInput)) {
            var cityCSVWithoutAccent = removeAccents(cityCSV);
            var cityInputWithoutAccent = removeAccents(cityInput);
            return cityCSVWithoutAccent.equalsIgnoreCase(cityInputWithoutAccent.trim());
        }
        return false;
    }

    public static String removeAccents(String input) {
        // Normalizza la stringa in modo da separare i caratteri dagli accenti
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Rimuove i caratteri diacritici (accents)
        return normalized.replaceAll("\\p{M}", "");
    }


}
