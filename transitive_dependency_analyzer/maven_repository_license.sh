#!/bin/bash 
BASE=$(echo $1 | awk -F, '{print $1}')
GROUP_ID=$(echo $BASE | awk -F: '{print $1}')
ARTIFACT_ID=$(echo $BASE | awk -F: '{print $2}')
VERSION=$(echo $BASE | awk -F: '{print $4}')
URL="https://mvnrepository.com/artifact/$GROUP_ID/$ARTIFACT_ID/$VERSION"
PROJECT_URL=$(curl $URL | grep "HomePage" | awk -FHomePage '{print $2}' | awk -Fhref= '{print $2}' | awk -F\> '{print $1}' | sed 's/\"//g')
echo $1,$PROJECT_URL
