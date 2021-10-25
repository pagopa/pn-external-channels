if ([ -z $1 ]) then
  destdir=physical
else 
  destdir=$1
fi

if ([ -z $2 ]) then
  filename=test_result_physical.csv
else 
  filename=$2
fi

destfilename="$(($(date +%s%N)/1000000))_$filename"

aws --endpoint-url=http://localhost:4566 s3 cp test_result_att_A.txt s3://external-channels-in/"$destdir"/test_result_att_A.txt

aws --endpoint-url=http://localhost:4566 s3 cp test_result_att_B.txt s3://external-channels-in/"$destdir"/test_result_att_B.txt

aws --endpoint-url=http://localhost:4566 s3 cp $filename s3://external-channels-in/"$destdir"/"$destfilename"
# aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url https://localhost:4566/000000000000/local-ext-channels-elab-res --message-body "{\"key\":\""$(($(date +%s%N)/1000000))_$filename"\"}"

echo "File "$filename" saved on "s3://external-channels-in/"$destdir"/"$destfilename"

read -n 1 -s -r -p "Press any key to continue"
