aws --endpoint-url=http://localhost:4566 sqs purge-queue --queue-url https://localhost:4566/000000000000/local-ext-channels-status
aws --endpoint-url=http://localhost:4566 sqs purge-queue --queue-url https://localhost:4566/000000000000/local-ext-channels-elab-res
aws --endpoint-url=http://localhost:4566 sqs purge-queue --queue-url https://localhost:4566/000000000000/local-ext-channels-inputs
aws --endpoint-url=http://localhost:4566 s3 rm s3://external-channels-in --recursive
aws --endpoint-url=http://localhost:4566 s3 rm s3://external-channels-out --recursive
echo "Queues and buckets are now empty"
read -n 1 -s -r -p "Press any key to continue"
