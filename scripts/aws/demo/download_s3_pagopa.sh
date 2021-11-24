rm -r in
rm -r out
aws --profile=pn-develop-extchn-pagopa s3 cp s3://pn-pagopa-develop-extchn-output ./in --recursive --region eu-central-1
aws --profile=pn-develop-extchn-pagopa s3 cp s3://pn-pagopa-develop-extchn-input ./out --recursive --region eu-central-1
echo "Files downloaded"

read -n 1 -s -r -p "Press any key to continue"
