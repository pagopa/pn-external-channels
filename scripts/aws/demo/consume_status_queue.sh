echo Starting status-message consumer
echo Press CTRL+C to quit

while sleep 1; do
    
RES="$(aws --endpoint-url=http://localhost:4566 sqs receive-message --max-number-of-messages 10 --queue-url https://localhost:4566/000000000000/local-ext-channels-status)"
if ([ ! -z "$RES" ]) then	
	echo "$RES"
	aws --endpoint-url=http://localhost:4566 sqs purge-queue --queue-url https://localhost:4566/000000000000/local-ext-channels-status
fi

echo "..."

done;