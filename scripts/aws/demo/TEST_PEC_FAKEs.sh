eventType="SEND_PEC_REQUEST"
attemptNum=1
mockBehaviour=fail-first
echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\"1\",\"iun\":\""$attemptNum"\",\"senderId\":\"3\",\"senderDenomination\":\"4\",\"senderPecAddress\":\"send@send.it\",\"recipientDenomination\":\"5\",\"recipientTaxId\":\"6\",\"pecAddress\":\"receive@"$mockBehaviour"\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$attemptNum"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

sleep 2s

eventType="SEND_PEC_REQUEST"
attemptNum=2
mockBehaviour=fail-first
echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\"1\",\"iun\":\""$attemptNum"\",\"senderId\":\"3\",\"senderDenomination\":\"4\",\"senderPecAddress\":\"send@send.it\",\"recipientDenomination\":\"5\",\"recipientTaxId\":\"6\",\"pecAddress\":\"receive@"$mockBehaviour"\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$attemptNum"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

sleep 2s

eventType="SEND_PEC_REQUEST"
attemptNum=3
mockBehaviour=fail-both
echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\"1\",\"iun\":\""$attemptNum"\",\"senderId\":\"3\",\"senderDenomination\":\"4\",\"senderPecAddress\":\"send@send.it\",\"recipientDenomination\":\"5\",\"recipientTaxId\":\"6\",\"pecAddress\":\"receive@"$mockBehaviour"\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$attemptNum"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

sleep 2s

eventType="SEND_PEC_REQUEST"
attemptNum=4
mockBehaviour=works
echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\"1\",\"iun\":\""$attemptNum"\",\"senderId\":\"3\",\"senderDenomination\":\"4\",\"senderPecAddress\":\"send@send.it\",\"recipientDenomination\":\"5\",\"recipientTaxId\":\"6\",\"pecAddress\":\"receive@"$mockBehaviour"\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$attemptNum"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"

sleep 2s

eventType="SEND_PEC_REQUEST"
attemptNum=5
mockBehaviour=realmail.it.real
echo "Sending "$eventType", attempt number "$attemptNum" with "$mockBehaviour" behaviour"
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs --message-body "{\"requestCorrelationId\":\"1\",\"iun\":\""$attemptNum"\",\"senderId\":\"3\",\"senderDenomination\":\"4\",\"senderPecAddress\":\"send@send.it\",\"recipientDenomination\":\"5\",\"recipientTaxId\":\"6\",\"pecAddress\":\"receive@"$mockBehaviour"\",\"shipmentDate\":\"2021-09-20T15:57:25.300Z\"}" --message-attributes "{\"eventType\":{\"DataType\":\"String\",\"StringValue\":\""$eventType"\"},\"publisher\":{\"DataType\":\"String\",\"StringValue\":\"pub\"},\"eventId\":{\"DataType\":\"String\",\"StringValue\":\"a23-"$attemptNum"\"},\"iun\":{\"DataType\":\"String\",\"StringValue\":\""$attemptNum"\"},\"createdAt\":{\"DataType\":\"String\",\"StringValue\":\"2021-09-20T15:57:25.097159300Z\"}}"
