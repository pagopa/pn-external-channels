package it.pagopa.pn.externalchannels.service.mockpostel;

import it.pagopa.pn.externalchannels.mock_postel.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@lombok.CustomLog
public class DeduplicaService {

	public DeduplicaService() {
	}

	public Mono<RisultatoDeduplica> deduplica(InputDeduplica request) {
		MasterIn masterIn = request.getMasterIn();
		SlaveIn slaveIn = request.getSlaveIn();

		if (areFieldsBlank(masterIn) || areFieldsBlank(slaveIn)) {
			return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Valorizzare i campi"));
		}

		if (hasServerError(masterIn, slaveIn)) {
			return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
		}

		convertFieldsToUpperCase(masterIn);
		convertFieldsToUpperCase(slaveIn);

		RisultatoDeduplica risultatoDeduplica = createRisultatoDeduplica(masterIn, slaveIn);

		if (areFieldsEqual(masterIn, slaveIn)) {
			setSuccessfulResult(risultatoDeduplica);
		} else {
			setFailedResult(risultatoDeduplica);
		}

		return Mono.just(risultatoDeduplica);
	}

	private boolean areFieldsBlank(MasterIn masterIn) {
		return StringUtils.isBlank(masterIn.getCap())
				|| StringUtils.isBlank(masterIn.getIndirizzo())
				|| StringUtils.isBlank(masterIn.getLocalita())
				|| StringUtils.isBlank(masterIn.getProvincia())
				|| StringUtils.isBlank(masterIn.getStato());
	}

	private boolean areFieldsBlank(SlaveIn slaveIn) {
		return StringUtils.isBlank(slaveIn.getCap())
				|| StringUtils.isBlank(slaveIn.getIndirizzo())
				|| StringUtils.isBlank(slaveIn.getLocalita())
				|| StringUtils.isBlank(slaveIn.getProvincia())
				|| StringUtils.isBlank(slaveIn.getStato());
	}

	private boolean hasServerError(MasterIn masterIn, SlaveIn slaveIn) {
		return masterIn.getCap().equals("500") || slaveIn.getCap().equals("500");
	}

	private void convertFieldsToUpperCase(MasterIn masterIn) {
		masterIn.setCap(masterIn.getCap().toUpperCase());
		masterIn.setIndirizzo(masterIn.getIndirizzo().toUpperCase());
		masterIn.setLocalita(masterIn.getLocalita().toUpperCase());
		masterIn.setProvincia(masterIn.getProvincia().toUpperCase());
		masterIn.setLocalitaAggiuntiva(masterIn.getLocalitaAggiuntiva().toUpperCase());
		masterIn.setStato(masterIn.getStato().toUpperCase());
	}

	private void convertFieldsToUpperCase(SlaveIn slaveIn) {
		slaveIn.setCap(slaveIn.getCap().toUpperCase());
		slaveIn.setIndirizzo(slaveIn.getIndirizzo().toUpperCase());
		slaveIn.setLocalita(slaveIn.getLocalita().toUpperCase());
		slaveIn.setProvincia(slaveIn.getProvincia().toUpperCase());
		slaveIn.setLocalitaAggiuntiva(slaveIn.getLocalitaAggiuntiva().toUpperCase());
		slaveIn.setStato(slaveIn.getStato().toUpperCase());
	}

	private RisultatoDeduplica createRisultatoDeduplica(MasterIn masterIn, SlaveIn slaveIn) {
		RisultatoDeduplica risultatoDeduplica = new RisultatoDeduplica();
		MasterOut masterOut = new MasterOut();
		SlaveOut slaveOut = new SlaveOut();
		masterOut.setsCap(masterIn.getCap());
		masterOut.setsViaCompletaAbb(masterIn.getIndirizzo());
		masterOut.setsStatoAbb(masterIn.getStato());
		masterOut.setsSiglaProv(masterIn.getProvincia());
		slaveOut.setsCap(slaveIn.getCap());
		slaveOut.setsViaCompletaAbb(slaveIn.getIndirizzo());
		slaveOut.setsStatoAbb(slaveIn.getStato());
		slaveOut.setsSiglaProv(slaveIn.getProvincia());
		risultatoDeduplica.setMasterOut(masterOut);
		risultatoDeduplica.setSlaveOut(slaveOut);
		return risultatoDeduplica;
	}

	private boolean areFieldsEqual(MasterIn masterIn, SlaveIn slaveIn) {
		return masterIn.getCap().equals(slaveIn.getCap())
				&& masterIn.getIndirizzo().equals(slaveIn.getIndirizzo())
				&& masterIn.getLocalita().equals(slaveIn.getLocalita())
				&& masterIn.getProvincia().equals(slaveIn.getProvincia())
				&& masterIn.getLocalitaAggiuntiva().equals(slaveIn.getLocalitaAggiuntiva())
				&& masterIn.getStato().equals(slaveIn.getStato());
	}

	private void setSuccessfulResult(RisultatoDeduplica risultatoDeduplica) {
		risultatoDeduplica.setRisultatoDedu("OK");
		risultatoDeduplica.setErroreDedu(0);
	}

	private void setFailedResult(RisultatoDeduplica risultatoDeduplica) {
		risultatoDeduplica.setRisultatoDedu("KO");
		risultatoDeduplica.setErroreDedu(1);
	}
	/*
	public Mono<RisultatoDeduplica> deduplica(InputDeduplica request){
		MasterIn masterIn = request.getMasterIn();
		SlaveIn slaveIn = request.getSlaveIn();
		if(StringUtils.isBlank(masterIn.getCap())
				|| StringUtils.isBlank(masterIn.getIndirizzo())
				|| StringUtils.isBlank(masterIn.getLocalita()) || StringUtils.isBlank(masterIn.getProvincia())
				|| StringUtils.isBlank(masterIn.getStato()) || StringUtils.isBlank(slaveIn.getCap())
				|| StringUtils.isBlank(slaveIn.getIndirizzo()) || StringUtils.isBlank(slaveIn.getLocalita())
				|| StringUtils.isBlank(slaveIn.getProvincia()) || StringUtils.isBlank(slaveIn.getStato())){
			return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Valorizzare i campi"));
		}
		if(masterIn.getCap().equals("500")||slaveIn.getCap().equals("500")){
			return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
		}
		slaveIn.setCap(slaveIn.getCap().toUpperCase());
		slaveIn.setIndirizzo(slaveIn.getIndirizzo().toUpperCase());
		slaveIn.setLocalita(slaveIn.getLocalita().toUpperCase());
		slaveIn.setProvincia(slaveIn.getProvincia().toUpperCase());
		slaveIn.setLocalitaAggiuntiva(slaveIn.getLocalitaAggiuntiva().toUpperCase());
		slaveIn.setStato(slaveIn.getStato().toUpperCase());
		masterIn.setCap(masterIn.getCap().toUpperCase());
		masterIn.setIndirizzo(masterIn.getIndirizzo().toUpperCase());
		masterIn.setLocalita(masterIn.getLocalita().toUpperCase());
		masterIn.setProvincia(masterIn.getProvincia().toUpperCase());
		masterIn.setLocalitaAggiuntiva(masterIn.getLocalitaAggiuntiva().toUpperCase());
		masterIn.setStato(masterIn.getStato().toUpperCase());
		RisultatoDeduplica risultatoDeduplica = new RisultatoDeduplica();
		MasterOut masterOut = new MasterOut();
		SlaveOut slaveOut = new SlaveOut();
		masterOut.setsCap(masterIn.getCap());
		masterOut.setsViaCompletaAbb(masterIn.getIndirizzo());
		masterOut.setsStatoAbb(masterIn.getStato());
		masterOut.setsSiglaProv(masterIn.getProvincia());
		slaveOut.setsCap(slaveIn.getCap());
		slaveOut.setsViaCompletaAbb(slaveIn.getIndirizzo());
		slaveOut.setsStatoAbb(slaveIn.getStato());
		slaveOut.setsSiglaProv(slaveIn.getProvincia());
		risultatoDeduplica.setMasterOut(masterOut);
		risultatoDeduplica.setSlaveOut(slaveOut);
		if (masterIn.getCap().equals(slaveIn.getCap()) &&
				masterIn.getIndirizzo().equals(slaveIn.getIndirizzo()) &&
				masterIn.getLocalita().equals(slaveIn.getLocalita()) &&
				masterIn.getProvincia().equals(slaveIn.getProvincia()) &&
				masterIn.getLocalitaAggiuntiva().equals(slaveIn.getLocalitaAggiuntiva()) &&
				masterIn.getStato().equals(slaveIn.getStato())) {
			risultatoDeduplica.setRisultatoDedu("OK");
			risultatoDeduplica.setErroreDedu(0);
			return Mono.just(risultatoDeduplica);
		} else {
			risultatoDeduplica.setRisultatoDedu("KO");
			risultatoDeduplica.setErroreDedu(1);
			return Mono.just(risultatoDeduplica);
		}

	}*/

}
