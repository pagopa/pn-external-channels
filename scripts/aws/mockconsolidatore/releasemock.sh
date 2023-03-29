#!/usr/bin/env bash -e

if ( [ $# -ne 4 ] ) then
  echo "Usage: $0 <aws-userprofile> <aws-region> <aws-bucketname> <target-env>"
  echo "<aws-userprofile>: AWS connection profile to the target AccountId"
  echo "<aws-region>: AWS region to deploy the stacks"
  echo "<aws-bucketname>: AWS Target Bucket for stack"
  echo "<target-env>: Target environment configuration"
  echo "z"

  if ( [ "$BASH_SOURCE" = "" ] ) then
    return 1
  else
    exit 1
  fi
fi

_AWS_USERPROFILE=$1
_AWS_REGION=$2
_BUCKETNAME=$3
_ENV=$4
_BUCKETPATH="mockconsolidatore"
_STACK="mockconsolidatore"
_WORKDIR="$PWD/target"
_PARAMSFILE="$_WORKDIR/$_STACK-params.yaml"

_AWSCLI_CMD_PREFIX="--profile $_AWS_USERPROFILE  --region $_AWS_REGION"

##### CLEAN WORKDIR
rm -rf $_WORKDIR
mkdir -p $_WORKDIR

##### COPY STACK
sed s/\!Sub\ \"\$\{TemplateBucketBaseUrl\}/\"./g $PWD/../cfn/microservice.yml > $_WORKDIR/$_STACK.yaml 
cp -r $PWD/fragments $_WORKDIR


##### BUILD PARAMS
echo "[" > $_PARAMSFILE.tmp
jq -r 'to_entries|map("\"\(.key)=\(.value)\"")|.[]' mockconsolidatore-$_ENV.json | tr '\n' ',' >> $_PARAMSFILE.tmp
sed '$ s/.$/\]/' $_PARAMSFILE.tmp > $_PARAMSFILE

##### STACK PARAMS
echo "=== CF Package"
aws $_AWSCLI_CMD_PREFIX \
    cloudformation package \
    --template-file "$_WORKDIR/$_STACK.yaml" \
    --output-template-file "$_WORKDIR/$_STACK.tmp" \
    --s3-bucket "$_BUCKETNAME" \
    --s3-prefix "$_BUCKETPATH"

##### STACK DEPLOY
echo "=== CF Deploy"
aws $_AWSCLI_CMD_PREFIX \
    cloudformation deploy \
    --stack-name $_STACK \
    --template-file "$_WORKDIR/$_STACK.tmp" \
    --capabilities CAPABILITY_NAMED_IAM \
    --parameter-overrides file://$_PARAMSFILE
