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
   `dopo 5s invio del C000 -> dopo 5s invio del C001 → dopo 5s invio del C007 → dopo 5s invio del C004`.
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
  “attempt_1”, il mock manderà la sequenza C000.5s-C001.5s-C005.5s-C008. Con la requestId contenente la parola
  “attempt_2”, il mock manderà la sequenza 5s-C000.5s-C001.5s-C005.5s-C003

## Come utilizzare il Mock per le notifiche cartacee
Il campo che viene preso in considerazione per l'invio di notifiche cartacee (`/external-channel/v1/paper-deliveries-engagements/:requestIdx`)
è receiverAddress. In particolare:

1. Se il campo receiverAddress contiene la stringa **@fail**, segue il seguente flusso di FALLIMENTO di invio: \
   `"001", "002", "003", "005" (ogni cinque secondi)`. \
   In più, se il receiverAddress contiene anche la stringa **discovered**, nell'ultima notifica inviata a DeliveryPush 
   (quella con codice 005) verrà valorizzato il campo discoveredAddress (corrispondente all'indirizzo del destinatario desunto dalle indagini del personale postale).
2. Altrimenti, segue il seguente flusso SUCCESSO: \
   `"001", "002", "003", "004" (ogni cinque secondi)`.

## Esempi di utilizzo del campo receiverAddressRow2 per l'invio di notifiche cartacee

- via Milano@fail => segue il flusso di fallimento
- via Milano => segue il flusso di successo

## Come utilizzare il Mock per le notifiche digitali di cortesia via SMS
Il campo che viene preso in considerazione per l'invio di notifiche di cortesia tramite SMS
(`/external-channels/v1/digital-deliveries/courtesy-simple-message-requests/:requestIdx`)
è receiverDigitalAddress. In particolare:

1. Se il campo receiverDigitalAddress inizia col numero **001**, segue lo stesso flusso di FALLIMENTO dell'invio digitale:
2. Altrimenti, segue lo stesso flusso di invio con SUCCESSO delle notifiche digitali.

## Esempi di utilizzo del campo receiverDigitalAddress per l'invio di notifiche tramite SMS

- 0013333333333 => segue il flusso di fallimento
- 3333333333333 => segue il flusso di successo

## Collection Postman

È possibile importarsi la seguente collection Postman per eseguire delle prove in locale:
[Collection Postman](MockExternalChannels.postman_collection.json)

