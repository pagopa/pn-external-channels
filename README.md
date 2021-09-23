Per configurare correttamente l'applicazione prima dell'uso, avviare in primis docker e lanciare il seguente comando da Cassandra Web

	- CREATE KEYSPACE external_channel WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} AND durable_writes = true;

A seguire, configurare il seguente profilo per l'AwsCli

	aws configure --profile default

		(output atteso / inserire i valori riportati sotto)
			AWS Access Key ID [None]: PN-TEST
			AWS Secret Access Key [None]: PN-TEST
			Default region name [None]: us-east-1
			Default output format [None]:

E lanciare la creazione delle code e i buckets AWS lanciando i seguenti scripts

 - create_queues.sh
 - create_buckets.sh

Infine, avviare l'applicativo e chiamare il seguente endpoint di sviluppo per la configurazione del template csv

 - POST http://localhost:8080/test-external-channel/cassandra/postCsvTemplate {"id":"1","idCsv":"8","description":"CSV per invio notifiche 890","columns":"[{\"name\":\"CODICE ATTO\",\"required\":\"true\",\"type\":\"text\",\"messageAttribute\":\"codiceAtto\",\"length\":20,\"note\":\"\"},{\"name\":\"DESTINATARIO\",\"required\":\"true\",\"type\":\"text\",\"messageAttribute\":\"recipientDenomination\",\"length\":44,\"note\":\"\"},{\"name\":\"PRESSO\",\"required\":\"false\",\"type\":\"text\",\"messageAttribute\":\"-\",\"length\":44,\"note\":\"\"},{\"name\":\"INDIRIZZO\",\"required\":\"true\",\"type\":\"text\",\"messageAttribute\":\"-\",\"length\":44,\"note\":\"\"},{\"name\":\"SPECIFICA INDIRIZZO\",\"required\":\"false\",\"type\":\"text\",\"messageAttribute\":\"-\",\"length\":44,\"note\":\"\"},{\"name\":\"CAP\",\"required\":\"true\",\"type\":\"paddedInt\",\"messageAttribute\":\"-\",\"length\":5,\"note\":\"\"},{\"name\":\"COMUNE\",\"required\":\"true\",\"type\":\"text\",\"messageAttribute\":\"-\",\"length\":35,\"note\":\"\"},{\"name\":\"PROVINCIA\",\"required\":\"true\",\"type\":\"text\",\"messageAttribute\":\"-\",\"length\":2,\"note\":\"\"},{\"name\":\"MODELLO STAMPA\",\"required\":\"true\",\"type\":\"text\",\"messageAttribute\":\"-\",\"length\":1,\"note\":\"\"},{\"name\":\"CONTO CORRENTE\",\"required\":\"false\",\"type\":\"paddedInt\",\"messageAttribute\":\"-\",\"length\":12,\"note\":\"\"},{\"name\":\"IMPORTO\",\"required\":\"false\",\"type\":\"currency\",\"messageAttribute\":\"-\",\"length\":10,\"note\":\"\"},{\"name\":\"SPESE NOTIFICA\",\"required\":\"false\",\"type\":\"currency\",\"messageAttribute\":\"-\",\"length\":10,\"note\":\"\"},{\"name\":\"IMPORTO DOPO 60GG\",\"required\":\"false\",\"type\":\"currency\",\"messageAttribute\":\"-\",\"length\":10,\"note\":\"\"},{\"name\":\"CODICE OBBLIGAZIONE\",\"required\":\"false\",\"type\":\"text\",\"messageAttribute\":\"-\",\"length\":18,\"note\":\"\"},{\"name\":\"CF DESTINATARIO\",\"required\":\"false\",\"type\":\"text\",\"messageAttribute\":\"recipientTaxId\",\"length\":16,\"note\":\"\"},{\"name\":\"PEC DESTINATARIO\",\"required\":\"false\",\"type\":\"text\",\"messageAttribute\":\"pecAddress\",\"length\":161,\"note\":\"\"},{\"name\":\"NUMERO CRONOLOGICO\",\"required\":\"false\",\"type\":\"text\",\"messageAttribute\":\"numeroCronologico\",\"length\":30,\"note\":\"\"},{\"name\":\"PARTE ISTANTE\",\"required\":\"false\",\"type\":\"text\",\"messageAttribute\":\"parteIstante\",\"length\":250,\"note\":\"\"},{\"name\":\"PROCURATORE\",\"required\":\"false\",\"type\":\"text\",\"messageAttribute\":\"procuratore\",\"length\":250,\"note\":\"\"},{\"name\":\"UFFICIALE GIUDIZIARIO\",\"required\":\"false\",\"type\":\"text\",\"messageAttribute\":\"ufficialeGiudiziario\",\"length\":250,\"note\":\"\"},{\"name\":\"ID PEC MITTENTE\",\"required\":\"true\",\"type\":\"text\",\"messageAttribute\":\"senderId\",\"length\":10,\"note\":\"\"},{\"name\":\"PEC MITTENTE\",\"required\":\"true\",\"type\":\"text\",\"messageAttribute\":\"senderPecAddress\",\"length\":161,\"note\":\"\"},{\"name\":\"ID TEMPLATE GESTORE PEC\",\"required\":\"true\",\"type\":\"text\",\"messageAttribute\":\"-\",\"length\":20,\"note\":\"\"},{\"name\":\"VERIFICA DOMICILIO DIGITALE\",\"required\":\"true\",\"type\":\"numeric\",\"messageAttribute\":\"-\",\"length\":1,\"note\":\"\"}]"}