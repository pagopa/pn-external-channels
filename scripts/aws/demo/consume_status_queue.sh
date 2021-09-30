echo Starting status-message consumer
echo Press CTRL+C to quit

while sleep 1; do
    
RES="$(aws --endpoint-url=http://localhost:4566 sqs receive-message --message-attribute-names All --max-number-of-messages 10 --queue-url https://localhost:4566/000000000000/local-ext-channels-outputs)"
if ([ ! -z "$RES" ]) then	
	echo "$RES"
	aws --endpoint-url=http://localhost:4566 sqs purge-queue --queue-url https://localhost:4566/000000000000/local-ext-channels-outputs
fi

echo "..."

done;