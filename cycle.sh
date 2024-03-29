#!/bin/bash

# String for process
PROC="d3ad"

# restart_process()
#
# Stop any existing process by the same name and then start a new one.
function restart_process {
  # If process is running
  res="$(ps ax | grep $PROC | grep -v grep)"
  if [ ! "${res:-null}" = null ]; then
    pid="$(echo $res | awk '{print $1}')"
    echo "Trying to kill process $pid"
    kill $pid
  fi
  echo "Trying to start process"
#  java -Xmx64M --illegal-access=permit -jar dist/d3ad.jar -c cfg/custom.json &
  java -Xmx64M -jar dist/d3ad.jar -c cfg/custom.json &
}

# Restart process by default
restart_process

# Infinite loop
while :
do
  # Fetch the latest changes
  git fetch
  # Check whether pull required
  if [ $(git rev-parse HEAD) != $(git rev-parse @{u}) ]; then
    # Pull the latest changes
    git pull
    # Rebuild the files
#    ant
    # Restart the process
    restart_process
  else
    echo "No changes"
  fi
  # Check if process is running
  res="$(ps ax | grep $PROC | grep -v grep)"
  if [ "${res:-null}" = null ]; then
    # As it's not running, rebuild and restart it
#    ant && restart_process
    restart_process
    # Do another loop shortly
    sleep 30
  else
    # Sleep for 5 minutes and check again
    sleep 300
  fi
done
