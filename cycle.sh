#!/bin/bash

# The user who has permission to pull the code
USER="user"

# restart_process()
#
# Stop any existing process by the same name and then start a new one.
function restart_process {
  kill "$(ps ax | grep d3ad | grep -v grep | awk '{print $1}')"
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
  # Sleep for 5 minutes and check again
  sleep 300
done
