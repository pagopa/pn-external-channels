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
    ""$endpoint"test-external-channel/job/trigger"

echo "Job Triggered"

read -n 1 -s -r -p "Press any key to continue"