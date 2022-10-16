#!/bin/bash

ROOT="$(pwd)"

# Clean dist directory
rm -rf dist
mkdir dist

# Compile Java classes
javac -Xlint:unchecked -source 1.7 -target 1.7 -d dist src/*.java

# Build the Jar executable
cd dist
  jar cfe d3ad.jar b.ds.Main **
cd $ROOT
