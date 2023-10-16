package it.pagopa.pn.externalchannels.service.mockpostel;

import it.pagopa.pn.externalchannels.mock_postel.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@lombok.CustomLog
public class DeduplicaService {
	public Mono<DeduplicaResponse> deduplica (DeduplicaRequest request) {
		AddressIn masterIn = request.getMasterIn();
		AddressIn slaveIn = request.getSlaveIn();
		DeduplicaResponse risultatoDeduplica;
		if (!StringUtils.isBlank(masterIn.getCap())) {
			switch (masterIn.getCap()) {
				case "00000":
					//ERRORE DI NORMALIZZAZIONE GESTISTO DA ADDRESS MANAGER IN 200
					risultatoDeduplica = createRisultatoDeduplica(slaveIn, 0, 1, null);
					break;
				case "11111":
					//ERRORE TECNICO GESTITO DA ADDRESS MANAGER IN 400
					risultatoDeduplica = createRisultatoDeduplica(slaveIn, 0, 1, "E1");
					break;
				case "00500":
					return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
				default:
					risultatoDeduplica = createRisultatoDeduplica(slaveIn, 1, null, null);
					break;
			}
		} else {
			risultatoDeduplica = createRisultatoDeduplica(slaveIn, 1, null, null);
		}

		if (areFieldsEqual(masterIn, slaveIn)) {
			setSuccessfulResult(risultatoDeduplica);
		} else {
			setFailedResult(risultatoDeduplica);
		}
		return Mono.just(risultatoDeduplica);
	}

	private void convertFieldsToUpperCase(AddressIn addressIn) {
		if (!StringUtils.isBlank(addressIn.getCap())) {
			addressIn.setCap(addressIn.getCap().toUpperCase());
		}
		if(!StringUtils.isBlank(addressIn.getIndirizzo())){
			addressIn.setIndirizzo(addressIn.getIndirizzo().toUpperCase());
		}
		if(!StringUtils.isBlank(addressIn.getLocalita())){
			addressIn.setLocalita(addressIn.getLocalita().toUpperCase());
		}
		if(!StringUtils.isBlank(addressIn.getLocalitaAggiuntiva())){
			addressIn.setLocalitaAggiuntiva(addressIn.getLocalitaAggiuntiva().toUpperCase());
		}
		if (!StringUtils.isBlank(addressIn.getProvincia())){
			addressIn.setProvincia(addressIn.getProvincia().toUpperCase());
		}
		if(!StringUtils.isBlank(addressIn.getStato())){
			addressIn.setStato(addressIn.getStato().toUpperCase());
		}
	}

	private DeduplicaResponse createRisultatoDeduplica (AddressIn slaveIn, Integer postalizzabile, Integer error, String erroreDedu) {
		DeduplicaResponse risultatoDeduplica = new DeduplicaResponse();
		convertFieldsToUpperCase(slaveIn);
		AddressOut slaveOut = getAddressOut(slaveIn, postalizzabile, error);
		risultatoDeduplica.setSlaveOut(slaveOut);
		risultatoDeduplica.setErrore(erroreDedu);
		return risultatoDeduplica;
	}

	@NotNull
	private static AddressOut getAddressOut(AddressIn addressIn, Integer postalizzabile, Integer error) {
		AddressOut addressOut = new AddressOut();
		addressOut.setnRisultatoNorm(1);
		addressOut.setfPostalizzabile(String.valueOf(postalizzabile));
		addressOut.setnErroreNorm(error);
		addressOut.setsCap(addressIn.getCap());
		addressOut.setsViaCompletaSpedizione(addressIn.getIndirizzo());
		addressOut.setsComuneSpedizione(addressIn.getLocalita());
		addressOut.setsFrazioneSpedizione(addressIn.getLocalitaAggiuntiva());
		addressOut.setsCivicoAltro("address2");
		addressOut.setsSiglaProv(addressIn.getProvincia());
		addressOut.setsStatoSpedizione(addressIn.getStato());
		return addressOut;
	}

	private boolean areFieldsEqual (AddressIn masterIn, AddressIn slaveIn) {
		return masterIn.getCap().equalsIgnoreCase(slaveIn.getCap())
				&& masterIn.getIndirizzo().equalsIgnoreCase(slaveIn.getIndirizzo())
				&& masterIn.getLocalita().equalsIgnoreCase(slaveIn.getLocalita())
				&& masterIn.getProvincia().equalsIgnoreCase(slaveIn.getProvincia())
				&& masterIn.getLocalitaAggiuntiva().equalsIgnoreCase(slaveIn.getLocalitaAggiuntiva())
				&& masterIn.getStato().equalsIgnoreCase(slaveIn.getStato());
	}

	private void setSuccessfulResult (DeduplicaResponse risultatoDeduplica) {
		risultatoDeduplica.setRisultatoDedu(Boolean.TRUE);
	}

	private void setFailedResult (DeduplicaResponse risultatoDeduplica) {
		risultatoDeduplica.setRisultatoDedu(Boolean.FALSE);
	}
}
