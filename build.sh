#!/bin/bash

JRE_TARGET="java-8"
JDK_TARGET="java-8"

# Source: https://stackoverflow.com/a/3232082/2847743
confirm() {
  # call with a prompt string or use a default
  read -r -p "${1:-Are you sure? [y/N]} " response
  case "$response" in
    [yY][eE][sS]|[yY])
      true
      ;;
    *)
      false
      ;;
  esac
}

main() {
  # Warning message about script
  echo "
-------------------------------------------------------------------------------
This script has been designed for use in Ubuntu. Please read the source code to
ensure the script is suitable for your needs. This script may cause damage to
your build environment if not correctly understood.
-------------------------------------------------------------------------------
"
  if ! $(confirm "Do you understand the risk? [y/N]"); then
    echo "[!!] You have indicated you do not understand, quitting"
    exit 1
  fi

  # Get Java target version
  PATH_JRE_TARGET="$(update-alternatives --list java  | grep $JRE_TARGET | head -1)"
  PATH_JDK_TARGET="$(update-alternatives --list javac | grep $JDK_TARGET | head -1)"
  # Make sure we got something for JRE
  if [ -z "$PATH_JRE_TARGET" ] || [ "$PATH_JRE_TARGET" == " " ]; then
    echo "[!!] Unable to set JRE target '$JRE_TARGET'"
    exit 1
  fi
  # Make sure we got something for JDK
  if [ -z "$PATH_JDK_TARGET" ] || [ "$PATH_JDK_TARGET" == " " ]; then
    echo "[!!] Unable to set JDK target '$JDK_TARGET'"
    exit 1
  fi
  # Let the user know what we are doing
  echo "[>>] Setting JRE to '$PATH_JRE_TARGET'"
  sudo update-alternatives --set java "$PATH_JRE_TARGET"
  echo "[>>] Setting JDK to '$PATH_JDK_TARGET'"
  sudo update-alternatives --set javac "$PATH_JDK_TARGET"

  # Run build
  echo "[>>] Building"
  ant clean
  ant
  # Run program
  echo "[>>] Running"
  java -jar dist/d3ad.jar -c cfg/default.json

  # Let the user know what we are doing
  echo "[>>] Resetting JRE to 'auto'"
  sudo update-alternatives --auto java
  echo "[>>] Resetting JDK to 'auto'"
  sudo update-alternatives --auto javac

  echo "[>>] Done"
  exit 0
}

main $@
