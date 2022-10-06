Per configurare correttamente l'applicazione prima dell'uso, avviare in primis docker 

Configurare il seguente profilo per l'AwsCli

	aws configure --profile localdev

		(output atteso / inserire i valori riportati sotto)
			AWS Access Key ID [None]: PN-TEST
			AWS Secret Access Key [None]: PN-TEST
			Default region name [None]: us-east-1
			Default output format [None]:

E lanciare la creazione delle code/buckets AWS e keyspace Cassandra lanciando il seguente script

 - 1_setup_environment.sh

Infine, avviare l'applicativo ed eseguire il seguente script per le configurazioni

 - 2_setup_application.sh


Istruzioni per la compilazione
```
    ./mvnw clean install
```
Istruzioni per il run
```
    ./mvnw spring-boot:run

