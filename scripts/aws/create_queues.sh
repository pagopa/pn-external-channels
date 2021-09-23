if ([ -z $1 ]) then
  profile=default
else 
  profile=$1
fi

queues="local-delivery-push-inputs local-delivery-push-actions local-ext-channels-inputs local-ext-channels-outputs"
for qn in  $( echo $queues | tr " " "\n" ) ; do
    echo creating $qn ...
    aws --profile $profile --region us-east-1 --endpoint-url http://localhost:4566 sqs create-queue \
        --queue-name $qn
done

queues="local-ext-channels-elab-res local-ext-channels-status local-ext-channels-error"
for qn in  $( echo $queues | tr " " "\n" ) ; do
    echo creating $qn ...
    aws --profile $profile --region us-east-1 --endpoint-url http://localhost:4566 sqs create-queue \
        --queue-name $qn
done
