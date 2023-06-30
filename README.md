# Mock di External Channels

Questo progetto è un mock dell'applicativo di External Channels, realizzato con Java 11 e Spring Boot.

Con questo mock è possibile, tramite opportuni parametri in input da passare alle REST APIs, di pilotare l'invio con
**SUCCESSO** o con **FALLIMENTO** di una notifica.

# Avvio del progetto in locale
1. eseguire `./mvnw clean install` per creare le classi autogenerate
2. avviare il main della classe Application

## Come utilizzare il Mock per le notifiche digitali (legals e mail)
Per l'API di invio notifica digitale,
(ad esempio `/external-channels/v1/digital-deliveries/legal-full-message-requests/:requestIdx`)
è possibile pilotare l'invio della notifica a seconda del campo **receiverDigitalAddress** della request,
nel seguente modo:

1. Per un dominio **@fail.xxxx** viene trattato l’invio come fallimento generico, con sequenza e tempistiche predefinite.
   In particolare, viene inviato il seguente flusso: \
   `dopo 5s invio del C000 -> dopo 5s invio del C001 → dopo 5s invio del C007 → dopo 5s invio del C004`.\
   un dominio **@fail.xxxx** inserito come pec di piattaforma (pec inserita su userAttributes) non comporterà il fallimento
   dell'inserimento (sarà valutato come **@ok.xxxx**), il **@fail.xxxx** verrà valutato solo in fase d'invio della notifica, 
   per testare il fallimento nell'inserimento della pec utilizzare **@failalways.xxxx**
2. Per un dominio **@xxxx** viene trattato l’invio come successo generico, con sequenza e tempistiche predefinite: \
   `dopo 5s invio del C000 -> dopo 5s invio del C001 → dopo 5s invio del C005 → dopo 5s invio del C003`.
3. Per un dominio **@sequence.xxxx** viene trattato l’invio secondo le tempistiche “programmate” dal dominio stesso
   secondo questa sintassi: \
   `durata-codice.durata-codice…….durata-codice_durata-codice.durata-codice…….durata-codice`.
    - Durata inteso come specifica del Duration, codice inteso come C000-C010, separati dal “.” da una eventuale altra coppia di durata-codice.
    - Con **“_”** invece, si va a separare eventuale comportamento diverso rispetto alla ricezione dello stesso invio per la
      stessa notifica (ricavando lo IUN dal requestId), così da poter comandare N° invii in maniera distinta.
    - Con **“attempt”**, si va a separare le sequenze dei macro tentativi che posso essere effettuati. Dalla requestId si
      può ricavare il numero attuale di tentativi (attempt). A quel punto viene eseguita la sequenza dell’attempt indicato nella requestId.

## Esempi di utilizzo del campo receiverDigitalAddress per l'invio di notifiche digitali

- **@sequence.5s-C000.5s-C001.5s-C005.5s-C003** => equivale all’invio ok
- **@sequence.5s-C000.5s-C001.5s-C007.5s-C004** => equivale all’invio KO
- **@sequence.3m-C008**  => equivale a tornare sempre C008 a ogni richiesta, con un ritardo di tre minuti
- **@sequence.5s-C008_5s-C008_5s-C008_5s-C000.5s-C001.5s-C005.5s-C003**  => equivale a tornare C008 per tre tentativi di invio
  distinti e poi d'inviare la sequenza dell’ok
- **@sequence.5s-C000.5s-C001.5s-C005.5s-C008attempt5s-C000.5s-C001.5s-C005.5s-C003** => con la requestId contenente
  “attempt_0”, il mock manderà la sequenza C000.5s-C001.5s-C005.5s-C008. Con la requestId contenente la parola
  “attempt_1”, il mock manderà la sequenza 5s-C000.5s-C001.5s-C005.5s-C003
  **@sequence.5s-C000.5s-C001.5s-C005.5s-C008attempt5s-C000.5s-C001.5s-C005.5s-C003attempt5s-C000.5s-C001.5s-C005.5s-C004** => con la requestId contenente
  “attempt_0”, il mock manderà la sequenza C000.5s-C001.5s-C005.5s-C008. Con la requestId contenente la parola
  “attempt_1”, il mock manderà la sequenza 5s-C000.5s-C001.5s-C005.5s-C003 per il tentativo di repeat 
-  “attempt_1”, il mock manderà la sequenza 5s-C000.5s-C001.5s-C005.5s-C004 per il secondo tentativo effettivo

- 
PS: essendo state modificate le regexp che necessitano di un suffisso corto, le sequence supportano SOLO il suffisso ".it", quindi ad esempio:
- **@sequence.5s-C000.5s-C001.5s-C005.5s-C003.it** => equivale all’invio ok

## Come utilizzare il Mock per le notifiche digitali di cortesia via SMS
Il campo che viene preso in considerazione per l'invio di notifiche di cortesia tramite SMS
(`/external-channels/v1/digital-deliveries/courtesy-simple-message-requests/:requestIdx`)
è receiverDigitalAddress. In particolare:

1. Se il campo receiverDigitalAddress inizia col numero **001**, segue lo stesso flusso di FALLIMENTO dell'invio digitale:
2. Altrimenti, segue lo stesso flusso di invio con SUCCESSO delle notifiche digitali.

## Esempi di utilizzo del campo receiverDigitalAddress per l'invio di notifiche tramite SMS

- 0013333333333 => segue il flusso di fallimento
- 3333333333333 => segue il flusso di successo


## Come utilizzare il Mock per le notifiche cartacee
Il campo che viene preso in considerazione per l'invio di notifiche cartacee (`/external-channel/v1/paper-deliveries-engagements/:requestIdx`)
è receiverAddress. In particolare:

1. L'indirizzo prevede il passaggio di una @sequence. Per il formato si veda il JSON di esempio, cmq la sintassi è
   1. **@sequence.5s-<codice>[comandi].10s-<codice>[comandi].20s-<codice>[comandi]** : 
      1. 5s: durata di attesa prima di invio del codice
      2. <codice>: il codice da spedire, esempio RECAG003D, senza < e >
      3. [comandi]: azioni aggiuntive per il codice, separate da ";". per ora supportate sono: DISCOVERY,DOC:< doctype >,DELAY:< duration >. 
         1. DISCOVERY invia l'eventuale indirizzo di discovery specificato in @discovered. 
         2. DOC invia un allegato con documentType=< doctype >.
         3. FAILCAUSE invia una deliveryFailureCause popolata con il valore specificato
         4. DELAY aggiunge al timestamp dell'evento la durata indicata in < duration > (NB: per default le durate sono negative. per aggiungere secondi usare esplicitamente +, quindi es DELAY:+1s). NB: lo stesso delay viene aggiunto ai DOC, salvo non sia esplicitato da DELAYDOC.
         5. DELAYDOC aggiunge al timestamp dei DOCUMENTI dell'evento la durata indicata in < duration > (NB: per default le durate sono negative. per aggiungere secondi usare esplicitamente +, quindi es DELAY:+1s)
   2. se contiene anche la stringa **@discovered**, il suo valore viene inviato come indirizzo nel codice con azione DISCOVERY 
2. Se il campo receiverAddress contiene la stringa **@fail** o **@ok**, cerca nel parameter store MapExternalChannelMockSequence la 
   sequenza corrispondente. Inoltre, se non specificata, cerca la sequenza per il productType richiesto (quindi ad esempio @fail_ar)
3. Specificare un indirizzo senza @sequence o @ok/fail, genera implicitamente un @ok


## Esempi di utilizzo del campo receiverAddress per l'invio di notifiche cartacee

- via Milano @fail => segue il flusso di fallimento per il productType specificato
- via Milano => segue il flusso di successo per il productType specificato

Per vedere altre stringhe possibili, fare riferimento a mocksequences.json

## Invio cartaceo, simulazione CONSOLIDATORE

Viene esposto anche un servizio di mock del consolidatore. Le regole con cui questo funziona sono le stesse del cartaceo.
L'endpoint però prevede di inviare gli eventi verso un webhook.
Tale webhook, può essere più di uno e va selezionato tramite l'header x-pagopa-extch-service-id.
La risoluzione di che endpoint è, avviente tramite la ricerca di un simple parameter MapExternalChannelMockServiceIdEndpoint, il formato è:
`[{
   "serviceId":"pn-cons-000",
   "endpoint":"http://localhost:8080",
   "endpointServiceId":"pn-extchannel-000"
},
{
   "serviceId":"pn-cons-001",
   "endpoint":"http://localhost:8081",
   "endpointServiceId":"pn-extchannel-001"
}
]`

Risolto l'endpoint, nell'invocazione del webhook verrà usato il serviceid specificato e (opzionalmente) una api-key.
L'api-key viene letta da
`**arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:pn-ExternalChannels-Secrets:ExternalChannelApiKey**`
e deve avere il formato json:

`[{"serviceId": "pn-cons-000", "apiKey":"1234567"}, {"serviceId": "pn-cons-001", "apiKey":"98765432"}]`


## Collection Postman

È possibile importarsi la seguente collection Postman per eseguire delle prove in locale:
[Collection Postman](MockExternalChannels.postman_collection.json)

