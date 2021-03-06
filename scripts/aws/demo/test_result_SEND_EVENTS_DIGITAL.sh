if ([ -z $1 ]) then
  eventType=SEND_PEC_REQUEST
else 
  eventType=$1
fi

if ([ -z $2 ]) then
  attemptNum="2"
else 
  attemptNum=$2
fi

if ([ -z $3 ]) then
  mockBehaviour="none.it"
else 
  mockBehaviour=$3
fi

iun=150

echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"accessUrl\":\"0\",\"requestCorrelationId\":\""$iun"\",\"iun\":\""$iun"\",\"senderId\":\"3\",\"senderDenomination\":\"PAGOPA\",\"senderPecAddress\":\"pagopa@pagopa.it\",\"recipientDenomination\":\"MARIO BIANCHI\",\"recipientTaxId\":\"BNCMRA70A01F205B\",\"pecAddress\":\"receive@"$mockBehaviour"\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\""$iun"-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$iun"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

iun=151

echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"accessUrl\":\"0\",\"requestCorrelationId\":\""$iun"\",\"iun\":\""$iun"\",\"senderId\":\"3\",\"senderDenomination\":\"PAGOPA\",\"senderPecAddress\":\"pagopa@pagopa.it\",\"recipientDenomination\":\"MARIO BIANCHI\",\"recipientTaxId\":\"BNCMRA70A01F205B\",\"pecAddress\":\"receive@"$mockBehaviour"\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\""$iun"-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$iun"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

iun=152

echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"accessUrl\":\"0\",\"requestCorrelationId\":\""$iun"\",\"iun\":\""$iun"\",\"senderId\":\"3\",\"senderDenomination\":\"PAGOPA\",\"senderPecAddress\":\"pagopa@pagopa.it\",\"recipientDenomination\":\"MARIO BIANCHI\",\"recipientTaxId\":\"BNCMRA70A01F205B\",\"pecAddress\":\"receive@"$mockBehaviour"\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\""$iun"-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$iun"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

read -n 1 -s -r -p "Press any key to continue"
