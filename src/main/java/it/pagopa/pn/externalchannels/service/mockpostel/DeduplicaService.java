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
		if (areFieldsBlank(masterIn) || areFieldsBlank(slaveIn)) {
			log.error("Valorizzare tutti i campi richiesti");
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valorizzare i campi"));
		}
		DeduplicaResponse risultatoDeduplica;
		if (!StringUtils.isBlank(masterIn.getCap())) {
			switch (masterIn.getCap()) {
				case "00000":
					risultatoDeduplica = createRisultatoDeduplica(masterIn, slaveIn, 0, 1);
					break;
				case "00500":
					return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
				default:
					risultatoDeduplica = createRisultatoDeduplica(masterIn, slaveIn, 1, null);
					break;
			}
		} else {
			risultatoDeduplica = createRisultatoDeduplica(masterIn, slaveIn, 1, null);
		}
		convertFieldsToUpperCase(masterIn);
		convertFieldsToUpperCase(slaveIn);

		if (areFieldsEqual(masterIn, slaveIn)) {
			setSuccessfulResult(risultatoDeduplica);
		} else {
			setFailedResult(risultatoDeduplica);
		}
		return Mono.just(risultatoDeduplica);
	}

	private boolean areFieldsBlank (AddressIn masterIn) {
		if (StringUtils.isBlank(masterIn.getStato())
				|| masterIn.getStato().toUpperCase().trim().startsWith("ITAL")) {
			return StringUtils.isBlank(masterIn.getCap())
					|| StringUtils.isBlank(masterIn.getIndirizzo())
					|| StringUtils.isBlank(masterIn.getLocalita())
					|| StringUtils.isBlank(masterIn.getProvincia());
		}
		return StringUtils.isBlank(masterIn.getIndirizzo())
				|| StringUtils.isBlank(masterIn.getLocalita())
				|| StringUtils.isBlank(masterIn.getProvincia())
				|| StringUtils.isBlank(masterIn.getStato());
	}

	private void convertFieldsToUpperCase (AddressIn masterIn) {
		if (!StringUtils.isBlank(masterIn.getCap())) {
			masterIn.setCap(masterIn.getCap().toUpperCase());
		}
		masterIn.setIndirizzo(masterIn.getIndirizzo().toUpperCase());
		masterIn.setLocalita(masterIn.getLocalita().toUpperCase());
		masterIn.setProvincia(masterIn.getProvincia().toUpperCase());
		masterIn.setLocalitaAggiuntiva(masterIn.getLocalitaAggiuntiva().toUpperCase());
		if(!StringUtils.isBlank(masterIn.getStato())){
			masterIn.setStato(masterIn.getStato().toUpperCase());
		}
	}

	private DeduplicaResponse createRisultatoDeduplica (AddressIn masterIn, AddressIn slaveIn, Integer postalizzabile, Integer error) {
		DeduplicaResponse risultatoDeduplica = new DeduplicaResponse();
		AddressOut masterOut = getAddressOut(masterIn, postalizzabile, error);
		AddressOut slaveOut = getAddressOut(slaveIn, postalizzabile, error);
		risultatoDeduplica.setMasterOut(masterOut);
		risultatoDeduplica.setSlaveOut(slaveOut);
		return risultatoDeduplica;
	}

	@NotNull
	private static AddressOut getAddressOut(AddressIn masterIn, Integer postalizzabile, Integer error) {
		AddressOut addressOut = new AddressOut();
		addressOut.setnRisultatoNorm(1);
		addressOut.setfPostalizzabile(String.valueOf(postalizzabile));
		addressOut.setnErroreNorm(error);
		addressOut.setsCap(masterIn.getCap());
		addressOut.setsViaCompletaSpedizione(masterIn.getIndirizzo());
		addressOut.setsStatoUff(masterIn.getStato());
		addressOut.setsComuneSpedizione(masterIn.getLocalita());
		addressOut.setsFrazioneSpedizione(masterIn.getLocalitaAggiuntiva());
		addressOut.setsCivicoAltro("address2");
		addressOut.setsSiglaProv(masterIn.getProvincia());
		return addressOut;
	}

	private boolean areFieldsEqual (AddressIn masterIn, AddressIn slaveIn) {
		return masterIn.getCap().equals(slaveIn.getCap())
				&& masterIn.getIndirizzo().equals(slaveIn.getIndirizzo())
				&& masterIn.getLocalita().equals(slaveIn.getLocalita())
				&& masterIn.getProvincia().equals(slaveIn.getProvincia())
				&& masterIn.getLocalitaAggiuntiva().equals(slaveIn.getLocalitaAggiuntiva())
				&& masterIn.getStato().equals(slaveIn.getStato());
	}

	private void setSuccessfulResult (DeduplicaResponse risultatoDeduplica) {
		risultatoDeduplica.setRisultatoDedu("1");
		risultatoDeduplica.setErroreDedu(0);
	}

	private void setFailedResult (DeduplicaResponse risultatoDeduplica) {
		risultatoDeduplica.setRisultatoDedu("0");
		risultatoDeduplica.setErroreDedu(1);
	}
}
