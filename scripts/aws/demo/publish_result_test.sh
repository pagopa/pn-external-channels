if ([ -z $1 ]) then
  filename=test_result.csv
else 
  filename=$1
fi

aws --endpoint-url=http://localhost:4566 s3 cp $filename s3://external-channels-in/"$(($(date +%s%N)/1000000))_$filename"
# aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-elab-res --message-body "{\"key\":\""$(($(date +%s%N)/1000000))_$filename"\"}"

echo "File "$filename" and notification sent"

read -n 1 -s -r -p "Press any key to continue"
