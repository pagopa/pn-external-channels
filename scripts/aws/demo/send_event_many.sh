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
  mockBehaviour=fail-first
else 
  mockBehaviour=$3
fi

echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\"1\",\"iun\":\"456\",\"senderId\":\"3\",\"senderDenomination\":\"4\",\"senderPecAddress\":\"send@send.it\",\"recipientDenomination\":\"5\",\"recipientTaxId\":\"6\",\"pecAddress\":\"receive@"$mockBehaviour"\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\"456\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"
echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\"1\",\"iun\":\"457\",\"senderId\":\"3\",\"senderDenomination\":\"4\",\"senderPecAddress\":\"send@send.it\",\"recipientDenomination\":\"5\",\"recipientTaxId\":\"6\",\"pecAddress\":\"receive@"$mockBehaviour"\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\"457\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"
echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\"1\",\"iun\":\"458\",\"senderId\":\"3\",\"senderDenomination\":\"4\",\"senderPecAddress\":\"send@send.it\",\"recipientDenomination\":\"5\",\"recipientTaxId\":\"6\",\"pecAddress\":\"receive@"$mockBehaviour"\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\"458\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

read -n 1 -s -r -p "Press any key to continue"
