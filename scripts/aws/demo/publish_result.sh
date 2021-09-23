if ([ -z $1 ]) then
  filename=file1
else 
  filename=$1
fi

echo "TEST" > $filename 

aws --endpoint-url=http://localhost:4566 s3 cp $filename s3://external-channels-in
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-elab-res --message-body "{\"key\":\""$filename"\"}"

rm $filename

echo "File "$filename" and notification sent"

read -n 1 -s -r -p "Press any key to continue"
