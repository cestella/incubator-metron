#!/bin/bash 
BASE=$(echo $1 | awk -F, '{print $1}')
GROUP_ID=$(echo $BASE | awk -F: '{print $1}')
ARTIFACT_ID=$(echo $BASE | awk -F: '{print $2}')
VERSION=$(echo $BASE | awk -F: '{print $4}')
URL="https://mvnrepository.com/artifact/$GROUP_ID/$ARTIFACT_ID/$VERSION"
LICENSE=$(curl $URL | grep "License" | grep "b lic" | awk -F\<td\> '{print $2}' | awk -Flic\"\> '{print $2}' | awk -F\< '{print $1}')
echo $BASE,$LICENSE
