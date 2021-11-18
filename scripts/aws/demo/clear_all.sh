if ([ -z $1 ]) then
  endpoint="http://localhost:8082/"
else 
  endpoint=$1
fi

if ([ -z $2 ]) then
  xapikey="none"
else 
  xapikey=$2
fi


curl -X GET -H "x-api-key: "$xapikey"" \
    ""$endpoint"external-channel/test/any/clear"

echo "Cassandra is now empty"

aws --endpoint-url=http://localhost:4566 sqs purge-queue --queue-url https://localhost:4566/000000000000/local-ext-channels-outputs
aws --endpoint-url=http://localhost:4566 sqs purge-queue --queue-url https://localhost:4566/000000000000/local-ext-channels-elab-res
aws --endpoint-url=http://localhost:4566 sqs purge-queue --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs
aws --endpoint-url=http://localhost:4566 s3 rm s3://external-channels-in --recursive
aws --endpoint-url=http://localhost:4566 s3 rm s3://external-channels-out --recursive
echo "Queues and buckets are now empty"
read -n 1 -s -r -p "Press any key to continue"
