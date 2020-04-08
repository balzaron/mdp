#!/bin/bash
_now=$(date +"%Y%m%d")
_logFile="./logs/mdp_$_now.log"
_errFile="./logs/mdp_$_now.err"

echo "Starting logging to $_logFile, $_errFile ..."
mkdir -p ./logs

java -jar mdp-admin-server-*.jar  1>$_logFile 2>$_errFile &
echo $! > mdp.pid

