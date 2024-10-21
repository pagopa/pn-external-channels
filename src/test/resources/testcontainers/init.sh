echo "### CREATE QUEUES ###"
queues="local-ext-channels-to-paper-outputs local-ext-channels-to-user-attributes-outputs local-ext-channels-to-internal-outputs"
for qn in  $( echo $queues | tr " " "\n" ) ; do
    echo creating queue $qn ...

    aws --profile default --region us-east-1 --endpoint-url http://localstack:4566 \
        sqs create-queue \
        --attributes '{"DelaySeconds":"2"}' \
        --queue-name $qn

done


echo "### START PARAMETER STORE CREATION FOR EXTERNAL CHANNEL MOCK ###"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    ssm put-parameter \
    --name "MapExternalChannelMockSequence" \
    --type String \
    --tier Advanced \
    --value "[
				{
					\"sequenceName\":\"OK_RS\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECRS001C\"
				},
				{
					\"sequenceName\":\"KO_RS\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECRS002A.5s-RECRS002B[Plico]-RECRS002C\"
				},
				{
					\"sequenceName\":\"OK-Retry_RS\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECRS006@retry.5s-CON080.5s-RECRS006@retry.5s-CON080.5s-RECRS001C\"
				},
				{
					\"sequenceName\":\"OK_AR\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECRN001A.5s-RECRN001B[AR].5s-RECRN001C\"
				},
				{
					\"sequenceName\":\"OK_890\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECAG001A.5s-RECAG001B[23L].5s-RECAG001C\"
				},
				{
					\"sequenceName\":\"FAIL_890\",
					\"sequence\":\"sequence":"@sequence.5s-CON080.5s-RECAG003A.5s-RECAG003B[DOC:23L;DOC:CAN].5s-RECAG003C\"
				},
				{
					\"sequenceName\":\"FAIL-Discovery_890\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECAG003D[DISCOVERY].5s-RECAG003E[DOC:Plico;DOC:Indagine].5s-RECAG003F@discovered.5s-CON080.5s-RECAG001A.5s-RECAG001B[DOC:23L].5s-RECAG001C\"
				},
				{
					\"sequenceName\":\"FAIL_AR\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECRN002A.5s-RECRN002B[DOC:Plico].5s-RECRN002C\"
				},
				{
					\"sequenceName\":\"OK-Giacenza-lte10_890\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECAG010.5s-RECAG011A.5s-RECAG012[DELAY:5d].5s-RECAG005A[DELAY:5d].5s-RECAG005B[DOC:ARCAD;DOC:23L;DELAY:5d].5s-RECAG005C[DELAY:5d]\"
				},
				{
					\"sequenceName\":\"OK-Giacenza-gt10_890\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECAG010.5s-RECAG011A.5s-RECAG012[DELAY:+10d].5s-RECAG011B[DOC:ARCAD;DOC:23L;DELAY:+10d].5s-RECAG005A[DELAY:+20d].5s-RECAG005C[DELAY:+20d]\"
				},
				{
					\"sequenceName\":\"FAIL-Giacenza-gt10_890\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECAG010.5s-RECAG011A.5s-RECAG012[DELAY:10d].5s-RECAG011B[DOC:ARCAD;DOC:23L;DELAY:10d].5s-RECAG007A[DELAY:30d].5s-RECAG007B[DOC:Plico;DELAY:30d].5s-RECAG007C[DELAY:30d]\"
				},
				{
					\"sequenceName\":\"FAIL-Giacenza-lte10_890\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECAG010.5s-RECAG011A.5s-RECAG012[DELAY:5d].5s-RECAG007A[DELAY:5d].5s-RECAG007B[DOC:ARCAD;DOC:Plico;DELAY:5d].5s-RECAG007C[DELAY:5d]\"
				},
				{
					\"sequenceName\":\"OK-CompiutaGiacenza_890\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECAG010.5s-RECAG011A.5s-RECAG012[DELAY:10d].5s-RECAG011B[DOC:ARCAD;DOC:23L;DELAY:10d].5s-RECAG008A[DELAY:60d].5s-RECAG008B[DOC:Plico;DELAY:60d].5s-RECAG008C[DELAY:60d]\"
				}

			]" \
    --overwrite

echo "### START PARAMETER STORE TWO CREATION FOR EXTERNAL CHANNEL MOCK ###"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    ssm put-parameter \
    --name "MapExternalChannelMockSequence2" \
    --type String \
    --tier Advanced \
    --value "[
				{
					\"sequenceName\":\"FAIL2-Irreperibile_890\",
					\"sequence\":\"@sequence.5s-CON996@retry.10s-CON080.5s-RECAG003D[FAILCAUSE:M03].5s-RECAG003E[DOC:Plico].5s-RECAG003F\"
				},
				{
          \"sequenceName\":\"OK_AR-CON020\",
          \"sequence\":\"@sequence.5s-CON080.5s-CON020[DOC:ZIP;PAGES:3].5s-CON018.5s-RECRN001A.5s-RECRN001B[DOC:AR;DELAY:1s].5s-RECRN001C\"
        }
			]" \
    --overwrite



echo "### START PARAMETER STORE CREATION FOR EXTERNAL CHANNEL MOCK ###"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    ssm put-parameter \
    --name "MapExternalChannelMockServiceIdEndpoint" \
    --type String \
    --overwrite \
    --value "[
				{
					\"serviceId\":\"pn-cons-000\",
					\"endpoint\":\"http://localhost:8082\",
					\"endpointServiceId\":\"pn-extchannel-000\"
				},
				{
					\"serviceId\":\"pn-cons-001\",
          \"endpoint\":\"http://localhost:8081\",
          \"endpointServiceId\":\"pn-extchannel-001\"
				}
			]"

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
  secretsmanager create-secret \
  --name pn-ExternalChannels-Secrets \
  --secret-string '{"ExternalChannelApiKey":"[{\"serviceId\": \"pn-cons-000\", \"apiKey\":\"1234567\"}, {\"serviceId\": \"pn-cons-001\", \"apiKey\":\"98765432\"}]"}'

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
  secretsmanager update-secret \
  --secret-id  "arn:aws:secretsmanager:us-east-1:000000000000:secret:pn-ExternalChannels-Secrets-HGesrW" \
  --secret-string '{"ExternalChannelApiKey":"[{\"serviceId\": \"pn-cons-000\", \"apiKey\":\"1234567\"}, {\"serviceId\": \"pn-cons-001\", \"apiKey\":\"98765432\"}]"}'

echo "### START DYNAMODB TABLE CREATION FOR EXTERNAL CHANNEL MOCK ###"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name ExtChannels \
    --attribute-definitions \
        AttributeName=iun,AttributeType=S \
        AttributeName=destinationAddress,AttributeType=S \
    --key-schema \
        AttributeName=iun,KeyType=HASH \
        AttributeName=destinationAddress,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name VerificationCode \
    --attribute-definitions \
        AttributeName=pk,AttributeType=S \
    --key-schema \
        AttributeName=pk,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5


aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name MockEcReceivedMessageTable \
    --attribute-definitions \
        AttributeName=requestId,AttributeType=S \
        AttributeName=iunRecIndex,AttributeType=S \
    --key-schema \
        AttributeName=requestId,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
        "[
            {
                \"IndexName\": \"iun-gsi\",
                \"KeySchema\": [{\"AttributeName\":\"iunRecIndex\",\"KeyType\":\"HASH\"}],
                \"Projection\":{
                    \"ProjectionType\":\"ALL\"
                },
                \"ProvisionedThroughput\": {
                    \"ReadCapacityUnits\": 10,
                    \"WriteCapacityUnits\": 5
                }
            }
        ]"


echo "Initialization terminated"
