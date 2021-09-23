echo "s3://external-channels-in"
aws --endpoint-url=http://localhost:4566 s3 ls s3://external-channels-in
echo "----"
echo "s3://external-channels-out"
aws --endpoint-url=http://localhost:4566 s3 ls s3://external-channels-out
echo "----"
echo
read -n 1 -s -r -p "Press any key to continue"
