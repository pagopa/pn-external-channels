rm -r in
rm -r out
aws --profile=pn-develop-extchn s3 cp s3://pn-develop-extchn-output ./in --recursive --region eu-south-1
aws --profile=pn-develop-extchn s3 cp s3://pn-develop-extchn-input ./out --recursive --region eu-south-1
echo "Files downloaded"

read -n 1 -s -r -p "Press any key to continue"
