echo "### CREATE QUEUES ###"
queues="local-ext-channels-to-paper-outputs"
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
    --overwrite \
    --value "[
				{
					\"sequenceName\":\"OK_RS\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECRS001C\"
				},
				{
					\"sequenceName\":\"KO_RS\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECRS002A.5s-RECRS002B[DOC:Plico]-RECRS002C\"
				},
				{
					\"sequenceName\":\"OK-Retry_RS\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECRS006@retry.5s-CON080.5s-RECRS006@retry.5s-CON080.5s-RECRS001C\"
				},
				{
					\"sequenceName\":\"OK_AR\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECRN001A.5s-RECRN001B[DOC:AR].5s-RECRN001C\"
				},
				{
					\"sequenceName\":\"OK_890\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECAG001A.5s-RECAG001B[DOC:23L].5s-RECAG001C\"
				},
				{
					\"sequenceName\":\"FAIL_890\",
					\"sequence\":\"@sequence.5s-CON080.5s-RECAG003A.5s-RECAG003B[DOC:23L;DOC:CAN].5s-RECAG003C\"
				},
				{
           \"sequenceName\":\"FAIL-Discovery_890\",
           \"sequence\":\"@sequence.5s-CON080.5s-RECAG003D[DISCOVERY].5s-RECAG003E[DOC:Plico;DOC:Indagine].5s-RECAG003F@discovered.5s-CON080.5s-RECAG001A.5s-RECAG001B[DOC:23L].5s-RECAG001C\"
        }
			]"



echo "Initialization terminated"
