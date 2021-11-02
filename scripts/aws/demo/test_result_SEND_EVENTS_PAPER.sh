if ([ -z $1 ]) then
  eventType=SEND_PAPER_REQUEST
else 
  eventType=$1
fi

if ([ -z $2 ]) then
  attemptNum="2"
else 
  attemptNum=$2
fi

if ([ -z $3 ]) then
  mockBehaviour=fail-first
else 
  mockBehaviour=$3
fi

iun=120

echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\""$iun"\",\"iun\":\""$iun"\",\"destinationAddress\":{\"at\":\"scala a\",\"address\":\"corso v emanuele 100\",\"addressDetails\":\"dettaglio\",\"zip\":\"80144\",\"municipality\":\"NAPOLI\",\"province\":\"NA\",\"foreignState\":\"\"},\"communicationType\":\"RECIEVED_DELIVERY_NOTICE\",\"serviceLevel\":\"REGISTERED_LETTER_890\",\"senderDenomination\":\"PAGOPA\",\"recipientDenomination\":\"Mario Rossi\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\""$iun"-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$iun"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

iun=121

echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\""$iun"\",\"iun\":\""$iun"\",\"destinationAddress\":{\"at\":\"scala a\",\"address\":\"corso v emanuele 100\",\"addressDetails\":\"dettaglio\",\"zip\":\"80144\",\"municipality\":\"NAPOLI\",\"province\":\"NA\",\"foreignState\":\"\"},\"communicationType\":\"RECIEVED_DELIVERY_NOTICE\",\"serviceLevel\":\"REGISTERED_LETTER_890\",\"senderDenomination\":\"PAGOPA\",\"recipientDenomination\":\"Mario Rossi\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\""$iun"-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$iun"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

iun=122

echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\""$iun"\",\"iun\":\""$iun"\",\"destinationAddress\":{\"at\":\"scala a\",\"address\":\"corso v emanuele 100\",\"addressDetails\":\"dettaglio\",\"zip\":\"80144\",\"municipality\":\"NAPOLI\",\"province\":\"NA\",\"foreignState\":\"\"},\"communicationType\":\"RECIEVED_DELIVERY_NOTICE\",\"serviceLevel\":\"REGISTERED_LETTER_890\",\"senderDenomination\":\"PAGOPA\",\"recipientDenomination\":\"Mario Rossi\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\""$iun"-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$iun"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

read -n 1 -s -r -p "Press any key to continue"
