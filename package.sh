#!/bin/bash

ROOT="$(pwd)"

# Clean dist directory
rm -rf dist
mkdir dist

# Compile Java classes
javac -Xlint:unchecked -source 1.8 -target 1.8 -d dist src/*.java

# Build the Jar executable
cd dist
  jar cfe d3ad.jar b.ds.Main **
cd $ROOT
