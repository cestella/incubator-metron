#!/bin/bash
OLD=( )
CURRENT=( )
while true;do
  if [ $# -eq 1 ];then
    CURRENT=( $( lsof -i -n -P | grep "[0-9]-" | awk '{print $1","$3","$9}' | sed 's/->/,/g' | sed 's/:/,/g' | sort | uniq ) )
  elif [ $# -eq 2 ]; then
    CURRENT=( $( lsof -i -n -P | grep $1 | grep "[0-9]-" | awk '{print $1","$3","$9}' | sed 's/->/,/g' | sed 's/:/,/g' | sort | uniq ) )
  else
    echo "Need 1 or two args"
    exit -1
  fi
  for line in $( echo ${CURRENT[@]} ${OLD[@]} | tr ' ' '\n' | sort | uniq -u );do
    echo $line
  done
  OLD=${CURRENT[@]}
  sleep 5 
done
