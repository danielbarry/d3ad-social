#!/bin/bash

# Infinite loop
while :
do
  # Fetch the latest changes
  git fetch
  # Check whether pull required
  if ! git diff --quiet remotes/origin/HEAD; then
    # Pull the latest changes
    git pull
    # Rebuild the files
    ant
    # Restart the process
    pkill "d3ad.jar"
    java -jar dist/d3ad.jar -c cfg/custom.json
  else
    echo "No changes"
  fi
  # Sleep for 5 minutes and check again
  sleep 300
done
