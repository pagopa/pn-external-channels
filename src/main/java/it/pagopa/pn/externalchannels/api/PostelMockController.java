package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.mock_postel.DeduplicaRequest;
import it.pagopa.pn.externalchannels.mock_postel.DeduplicaResponse;
import it.pagopa.pn.externalchannels.mock_postel.NormalizzazioneRequest;
import it.pagopa.pn.externalchannels.mock_postel.NormalizzazioneResponse;
import it.pagopa.pn.externalchannels.service.mockpostel.DeduplicaService;
import it.pagopa.pn.externalchannels.service.mockpostel.PostelService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
@lombok.CustomLog
public class PostelMockController implements DefaultApi {

    private final Scheduler scheduler;
    private final PostelService postelService;
    private final DeduplicaService deduplicaService;

    public PostelMockController(@Qualifier("externalChannelsScheduler") Scheduler scheduler,
                                PostelService postelService, DeduplicaService deduplicaService) {
        this.scheduler = scheduler;
        this.postelService = postelService;
        this.deduplicaService = deduplicaService;
    }

    /**
     * POST /send-normalizzatore-ingress/v1/normalizzazione : PN richiede la normalizzazione batch
     * Il file in ingresso conterrà i seguenti campi gestiti con logica posizionale (nessuna intestazione, separatore &#39;;&#39;):   - IdCodiceCliente : Id del cliente   - Provincia : Sigla Provincia - opzionale   - Cap : cap - opzionale   - localita : località/comune - obbligatorio   - localitaAggiuntiva : frazione - opzionale   - indirizzo : svia - obbligatorio - contiene la via completa DUG + TOPONIMO + CIVICO   - stato : sstato - opzionale Il processo di normalizzazione creerà un file di output contenente i seguenti campi gestiti con logica posizionale (nessuna intestazione, separatore &#39;;&#39;):   - IDCODICECLIENTE : Id del cliente;   - NRISULTATONORM : Risultato di normalizzazione (0 : scartato/ 1,2,3,4,5 : normalizzato);   - FPOSTALIZZABILE (0 : NON Postalizzabile, 1 : Postalizzabile);   - NERRORENORM : Codice di errore;   - SSIGLAPROV : Sigla provincia normalizzata;   - SSTATOUFF : Stato normalizzato (Valorizzato ITALIA, REPUBBLICA DI SAN MARINO e CITTA’ DEL VATICANO + TUTTI GLI STATI ESTERI);   - SSTATOABB : Stato abbreviato normalizzato;   - SSTATOSPEDIZIONE : Stato di Spedizione;   - SCOMUNEUFF : Comune normalizzato;   - SCOMUNEABB : Comune Abbreviato normalizzato;   - SCOMUNESPEDIZIONE : Comune di spedizione;   - SFRAZIONEUFF : Frazione normalizzata;   - SFRAZIONEABB : Frazione Abbreviata normalizzata;   - SFRAZIONESPEDIZIONE : Frazione di Spedizione;   - SCIVICOALTRO : altri elementi del civico (interno, piano, scala, palazzo …) - VA IN INDIRIZZO 2   - SCAP : cap normalizzato;   - SPRESSO : informazioni di presso e casella postale (C.P 123, Presso sig. rossi …) -  VA IN NAME 2;   - SVIACOMPLETAUFF : via completa normalizzata (DUG+COMPL+TOPONIMO+CIVICO POSTALE) ;   - SVIACOMPLETAABB: via completa normalizzata abbreviata (DUG+COMPL+TOPONIMO+CIVICO POSTALE ABBREVIATA) ;   - SVIACOMPLETASPEDIZIONE : Indirizzo di Stampa;
     *
     * @param pnAddressManagerCxId  (required)
     * @param xApiKey Credenziale di accesso (required)
     * @param normalizzazioneRequest  (required)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<NormalizzazioneResponse>> normalizzazione(String pnAddressManagerCxId, String xApiKey,
                                                                         Mono<NormalizzazioneRequest> normalizzazioneRequest, ServerWebExchange exchange) {
        return normalizzazioneRequest
                .flatMap(request -> {
                    log.info("requestId: {}", request.getRequestId());
                    return postelService.activateNormalizer(request);
                })
                .map(normalizzazioneResponse -> ResponseEntity.ok().body(normalizzazioneResponse))
                .publishOn(scheduler);
    }


    /**
     * POST /send-normalizzatore-ingress/v1/deduplica : PN richiede la deduplica online
     *
     * @param pnAddressManagerCxId  (required)
     * @param xApiKey Credenziale di accesso (required)
     * @param inputDeduplica  (required)
     * @return OK (status code 200)
     *         or Bad request (status code 400)
     *         or Internal error (status code 500)
     */
    @Override
    public Mono<ResponseEntity<DeduplicaResponse>> deduplica(String pnAddressManagerCxId, String xApiKey,
                                                             Mono<DeduplicaRequest> inputDeduplica, ServerWebExchange exchange) {
        return inputDeduplica
                .flatMap(deduplicaService::deduplica)
                .map(deduplicateResponse -> ResponseEntity.ok().body(deduplicateResponse))
                .publishOn(scheduler);
    }
}
