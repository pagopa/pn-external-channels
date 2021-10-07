rm -r in
rm -r out
aws --endpoint-url=http://localhost:4566 s3 cp s3://external-channels-in ./in --recursive
aws --endpoint-url=http://localhost:4566 s3 cp s3://external-channels-out ./out --recursive
echo "Files downloaded"

read -n 1 -s -r -p "Press any key to continue"
