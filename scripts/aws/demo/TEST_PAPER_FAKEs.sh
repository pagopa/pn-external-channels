eventType="SEND_PAPER_REQUEST"
attemptNum=6
mockBehaviour="ImmediateResponse(OK)"
echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\"6\",\"iun\":\""$attemptNum"\",\"communicationType\":\"FAILED_DELIVERY_NOTICE\",\"serviceLevel\":\"SIMPLE_REGISTERED_LETTER\",\"destinationAddress\":{\"at\":\"casa\",\"address\":\""$mockBehaviour"\",\"addressDetails\":\"scala a\",\"zip\":\"80131\",\"municipality\":\"NAPOLI\",\"province\":\"NA\",\"foreignState\":\"\"},\"senderId\":\"1\",\"senderDenomination\":\"2\",\"senderPecAddress\":\"pagopa@pagopa.it\",\"recipientDenomination\":\"Mario Rossi\",\"recipientTaxId\":\"MRRSS111\",\"pecAddress\":\"mario@rossi.it\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$attemptNum"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

sleep 2s

eventType="SEND_PAPER_REQUEST"
attemptNum=7
mockBehaviour="ImmediateResponse(NEW_ADDR:viaTest123)"
echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\"6\",\"iun\":\""$attemptNum"\",\"communicationType\":\"FAILED_DELIVERY_NOTICE\",\"serviceLevel\":\"SIMPLE_REGISTERED_LETTER\",\"destinationAddress\":{\"at\":\"casa\",\"address\":\""$mockBehaviour"\",\"addressDetails\":\"scala a\",\"zip\":\"80131\",\"municipality\":\"NAPOLI\",\"province\":\"NA\",\"foreignState\":\"\"},\"senderId\":\"1\",\"senderDenomination\":\"2\",\"senderPecAddress\":\"pagopa@pagopa.it\",\"recipientDenomination\":\"Mario Rossi\",\"recipientTaxId\":\"MRRSS111\",\"pecAddress\":\"mario@rossi.it\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$attemptNum"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

sleep 2s

eventType="SEND_PAPER_REQUEST"
attemptNum=8
mockBehaviour="ImmediateResponse(FAIL)"
echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\"6\",\"iun\":\""$attemptNum"\",\"communicationType\":\"FAILED_DELIVERY_NOTICE\",\"serviceLevel\":\"SIMPLE_REGISTERED_LETTER\",\"destinationAddress\":{\"at\":\"casa\",\"address\":\""$mockBehaviour"\",\"addressDetails\":\"scala a\",\"zip\":\"80131\",\"municipality\":\"NAPOLI\",\"province\":\"NA\",\"foreignState\":\"\"},\"senderId\":\"1\",\"senderDenomination\":\"2\",\"senderPecAddress\":\"pagopa@pagopa.it\",\"recipientDenomination\":\"Mario Rossi\",\"recipientTaxId\":\"MRRSS111\",\"pecAddress\":\"mario@rossi.it\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$attemptNum"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"


read -n 1 -s -r -p "Press any key to continue"
