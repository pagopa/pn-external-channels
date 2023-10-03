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
	public Mono<RisultatoDeduplica> deduplica (InputDeduplica request) {
		MasterIn masterIn = request.getMasterIn();
		SlaveIn slaveIn = request.getSlaveIn();
		if (areFieldsBlank(masterIn) || areFieldsBlank(slaveIn)) {
			return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Valorizzare i campi"));
		}
		RisultatoDeduplica risultatoDeduplica;
		if (!StringUtils.isBlank(masterIn.getCap())) {
			switch (masterIn.getCap()) {
				case "00000":
					risultatoDeduplica = createRisultatoDeduplica(masterIn, slaveIn, 0, 1);
					break;
				case "55555":
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

	private boolean areFieldsBlank (MasterIn masterIn) {
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

	private boolean areFieldsBlank (SlaveIn slaveIn) {
		if (StringUtils.isBlank(slaveIn.getStato())
				|| slaveIn.getStato().toUpperCase().trim().startsWith("ITAL")) {
			return StringUtils.isBlank(slaveIn.getCap())
					|| StringUtils.isBlank(slaveIn.getIndirizzo())
					|| StringUtils.isBlank(slaveIn.getLocalita())
					|| StringUtils.isBlank(slaveIn.getProvincia());
		}
		return StringUtils.isBlank(slaveIn.getIndirizzo())
				|| StringUtils.isBlank(slaveIn.getLocalita())
				|| StringUtils.isBlank(slaveIn.getProvincia())
				|| StringUtils.isBlank(slaveIn.getStato());
	}

	private void convertFieldsToUpperCase (MasterIn masterIn) {
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
	private void convertFieldsToUpperCase (SlaveIn slaveIn) {
		if (!StringUtils.isBlank(slaveIn.getCap())) {
			slaveIn.setCap(slaveIn.getCap().toUpperCase());
		}
		slaveIn.setIndirizzo(slaveIn.getIndirizzo().toUpperCase());
		slaveIn.setLocalita(slaveIn.getLocalita().toUpperCase());
		slaveIn.setProvincia(slaveIn.getProvincia().toUpperCase());
		slaveIn.setLocalitaAggiuntiva(slaveIn.getLocalitaAggiuntiva().toUpperCase());
		if (!StringUtils.isBlank(slaveIn.getStato())) {
			slaveIn.setStato(slaveIn.getStato().toUpperCase());
		}
	}
	private RisultatoDeduplica createRisultatoDeduplica (MasterIn masterIn, SlaveIn slaveIn, Integer postalizzabile, Integer error) {
		RisultatoDeduplica risultatoDeduplica = new RisultatoDeduplica();
		MasterOut masterOut = getMasterOut(masterIn, postalizzabile, error);
		SlaveOut slaveOut = getSlaveOut(slaveIn, postalizzabile, error);
		risultatoDeduplica.setMasterOut(masterOut);
		risultatoDeduplica.setSlaveOut(slaveOut);
		return risultatoDeduplica;
	}

	@NotNull
	private static SlaveOut getSlaveOut (SlaveIn slaveIn, Integer postalizzabile, Integer error) {
		SlaveOut slaveOut = new SlaveOut();
		slaveOut.setnRisultatoNorm(1);
		slaveOut.setfPostalizzabile(String.valueOf(postalizzabile));
		slaveOut.setnErroreNorm(error);
		slaveOut.setsCap(slaveIn.getCap());
		slaveOut.setsViaCompletaSpedizione(slaveIn.getIndirizzo());
		slaveOut.setsStatoUff(slaveIn.getStato());
		slaveOut.setsComuneSpedizione(slaveIn.getLocalita());
		slaveOut.setsFrazioneSpedizione(slaveIn.getLocalitaAggiuntiva());
		slaveOut.setsCivicoAltro("address2");
		slaveOut.setsSiglaProv(slaveIn.getProvincia());
		return slaveOut;
	}

	@NotNull
	private static MasterOut getMasterOut (MasterIn masterIn, Integer postalizzabile, Integer error) {
		MasterOut masterOut = new MasterOut();
		masterOut.setnRisultatoNorm(1);
		masterOut.setfPostalizzabile(String.valueOf(postalizzabile));
		masterOut.setnErroreNorm(error);
		masterOut.setsCap(masterIn.getCap());
		masterOut.setsViaCompletaSpedizione(masterIn.getIndirizzo());
		masterOut.setsStatoUff(masterIn.getStato());
		masterOut.setsComuneSpedizione(masterIn.getLocalita());
		masterOut.setsFrazioneSpedizione(masterIn.getLocalitaAggiuntiva());
		masterOut.setsCivicoAltro("address2");
		masterOut.setsSiglaProv(masterIn.getProvincia());
		return masterOut;
	}

	private boolean areFieldsEqual (MasterIn masterIn, SlaveIn slaveIn) {
		return masterIn.getCap().equals(slaveIn.getCap())
				&& masterIn.getIndirizzo().equals(slaveIn.getIndirizzo())
				&& masterIn.getLocalita().equals(slaveIn.getLocalita())
				&& masterIn.getProvincia().equals(slaveIn.getProvincia())
				&& masterIn.getLocalitaAggiuntiva().equals(slaveIn.getLocalitaAggiuntiva())
				&& masterIn.getStato().equals(slaveIn.getStato());
	}

	private void setSuccessfulResult (RisultatoDeduplica risultatoDeduplica) {
		risultatoDeduplica.setRisultatoDedu("1");
		risultatoDeduplica.setErroreDedu(0);
	}

	private void setFailedResult (RisultatoDeduplica risultatoDeduplica) {
		risultatoDeduplica.setRisultatoDedu("0");
		risultatoDeduplica.setErroreDedu(1);
	}
}
