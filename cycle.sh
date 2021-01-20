#!/bin/bash

# String for process
PROC="d3ad"
# The user who has permission to pull the code
USER="user"

# restart_process()
#
# Stop any existing process by the same name and then start a new one.
function restart_process {
  # If process is running
  res="$(ps ax | grep $PROC | grep -v grep)"
  if [ ! "${res:-null}" = null ]; then
    echo "Trying to kill process"
    kill "$(ps ax | grep $PROC | grep -v grep | awk '{print $1}')"
  fi
  echo "Trying to start process"
  java -jar dist/d3ad.jar -c cfg/custom.json &
}

# Restart process by default
restart_process

# Infinite loop
while :
do
  # Fetch the latest changes
  sudo -u $USER git fetch
  # Check whether pull required
  if ! git diff --quiet remotes/origin/HEAD; then
    # Pull the latest changes
    sudo -u $USER git pull
    # Rebuild the files
    ant
    # Restart the process
    restart_process
  else
    echo "No changes"
  fi
  # Check if process is running
  res="$(ps ax | grep $PROC | grep -v grep)"
  if [ "${res:-null}" = null ]; then
    # As it's not running, restart it
    restart_process
    # Do another loop shortly
    sleep 30
  else
    # Sleep for 5 minutes and check again
    sleep 300
  fi
done
