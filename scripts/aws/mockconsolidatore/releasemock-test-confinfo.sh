#!/usr/bin/env bash -e

if ( [ $# -ne 1 ] ) then
  echo "Usage: $0 <aws-userprofile> "
  echo "<aws-userprofile>: AWS connection profile to the target AccountId"
  echo "z"

  if ( [ "$BASH_SOURCE" = "" ] ) then
    return 1
  else
    exit 1
  fi
fi


_AWS_USERPROFILE=$1
_AWS_REGION="eu-south-1"
_BUCKETNAME="cd-confinfo-pipeline-cdartifactbucket-1qfs8fmbi6cw"
_ENV="test-confinfo"

CWD=$(pwd -P)

$CWD/releasemock.sh $_AWS_USERPROFILE $_AWS_REGION $_BUCKETNAME $_ENV