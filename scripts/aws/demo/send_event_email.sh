if ([ -z $1 ]) then
  eventType=SEND_COURTESY_EMAIL
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

iun=$(date +%s)

echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"iun\":\""$iun"\",\"senderId\":\"456\",\"senderDenomination\":\"PAGOPA\",\"senderEmailAddress\":\"piattaformanotifica.poc@gmail.com\",\"recipientDenomination\":\"MARIO ROSSI\",\"recipientTaxId\":\"MRRSS123\",\"emailAddress\":\"piattaformanotifica.poc@gmail.com\",\"shipmentDate\":\"2021-09-20T15:57:25.097159300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$iun"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

read -n 1 -s -r -p "Press any key to continue"
