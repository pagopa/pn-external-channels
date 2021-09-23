if ([ -z $1 ]) then
  profile=default
else 
  profile=$1
fi

queues="external-channels-out external-channels-in"
for qn in  $( echo $queues | tr " " "\n" ) ; do
    echo creating $qn ...
    aws --profile $profile --region us-east-1 --endpoint-url http://localhost:4566 s3 mb s3://$qn
done
