package it.pagopa.pn.externalchannels.service.mockpostel;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.externalchannels.mock_postel.*;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@CustomLog
@RequiredArgsConstructor
public class DeduplicaService {

    private final AddressUtils addressUtils;

    public Mono<DeduplicaResponse> deduplica(DeduplicaRequest request) {
        AddressIn masterIn = request.getMasterIn();
        AddressIn slaveIn = request.getSlaveIn();
        DeduplicaResponse risultatoDeduplica;

        boolean isItalian = addressUtils.isItalian(slaveIn.getStato());
        if (isItalian) {
            switch (slaveIn.getCap()) {
                case "00000":
                    //ERRORE DI NORMALIZZAZIONE GESTISTO DA ADDRESS MANAGER IN 200
                    risultatoDeduplica = createRisultatoDeduplica(slaveIn, masterIn, 0, 441, null);
                    break;
                case "11111":
                    //ERRORE TECNICO GESTITO DA ADDRESS MANAGER IN 400
                    risultatoDeduplica = createRisultatoDeduplica(slaveIn, masterIn, 0, null, "DED400");
                    break;
                case "22222":
                    //ERRORE MASTER SCARTATO GESTITO DA ADDRESS MANAGER
                    risultatoDeduplica = createRisultatoDeduplica(slaveIn, masterIn, 1, null, "DED001");
                    break;
                case "33333":
                    //ERRORE NON POSTALIZZABILE E SLAVE SCARTATO GESTITO DA ADDRESS MANAGER
                    risultatoDeduplica = createRisultatoDeduplica(slaveIn, masterIn, 0, 443, "DED002");
                    break;
                case "00500":
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
                default:
                    try {
                        addressUtils.verifyCapAndCity(slaveIn.getCap(), slaveIn.getProvincia(), slaveIn.getLocalita());
                        risultatoDeduplica = createRisultatoDeduplica(slaveIn, masterIn, 1, null, null);
                    } catch (PnInternalException e) {
                        risultatoDeduplica = createRisultatoDeduplica(slaveIn, masterIn, 1, 19, null);
                    }
                    break;
            }
        } else {
            try {
                addressUtils.searchCountry(slaveIn.getStato());
                risultatoDeduplica = createRisultatoDeduplica(slaveIn, masterIn, 1, null, null);
            } catch (PnInternalException e) {
                risultatoDeduplica = createRisultatoDeduplica(slaveIn, masterIn, 1, 601, null);
            }
        }

        boolean areEquals = addressUtils.compareAddress(masterIn, slaveIn, isItalian);
        if (areEquals) {
            setSuccessfulResult(risultatoDeduplica);
        } else {
            setFailedResult(risultatoDeduplica);
        }

        return Mono.just(risultatoDeduplica);
    }

    private DeduplicaResponse createRisultatoDeduplica(AddressIn slaveIn, AddressIn masterIn, Integer postalizzabile, Integer error, String erroreDedu) {
        DeduplicaResponse risultatoDeduplica = new DeduplicaResponse();
        AddressIn normalizedAddress = addressUtils.normalizeAddress(slaveIn);
        AddressOut slaveOut = buildAddressOut(normalizedAddress, postalizzabile, error);
        AddressOut masterOut = buildAddressOut(masterIn, 1, null);
        risultatoDeduplica.setSlaveOut(slaveOut);
        risultatoDeduplica.setMasterOut(masterOut);
        risultatoDeduplica.setErrore(erroreDedu);
        return risultatoDeduplica;
    }

    @NotNull
    private static AddressOut buildAddressOut(AddressIn addressIn, Integer postalizzabile, Integer error) {
        AddressOut addressOut = new AddressOut();
        addressOut.setId(addressIn.getId());
        addressOut.setnRisultatoNorm(1);
        addressOut.setfPostalizzabile(String.valueOf(postalizzabile));
        addressOut.setnErroreNorm(error);
        addressOut.setsCap(addressIn.getCap());
        addressOut.setsViaCompletaSpedizione(addressIn.getIndirizzo());
        addressOut.setsComuneSpedizione(addressIn.getLocalita());
        addressOut.setsFrazioneSpedizione(addressIn.getLocalitaAggiuntiva());
        addressOut.setsCivicoAltro(addressIn.getIndirizzoAggiuntivo());
        addressOut.setsSiglaProv(addressIn.getProvincia());
        addressOut.setsStatoSpedizione(addressIn.getStato());
        return addressOut;
    }

    private void setSuccessfulResult(DeduplicaResponse risultatoDeduplica) {
        risultatoDeduplica.setRisultatoDedu(Boolean.TRUE);
    }

    private void setFailedResult(DeduplicaResponse risultatoDeduplica) {
        risultatoDeduplica.setRisultatoDedu(Boolean.FALSE);
    }
}
