#!/bin/bash

OUTPUT_FILE=testng-fileshub-package.lst
SRC_DIR=./../src



# Display all directories and its sub-directories
du  ${SRC_DIR=} | \

  # Remove characters up to 'src' pattern  
  sed "s/.*src//" | \
  
  # Retain lines pattern '/test/'
  grep '/test' | \
  
  # Replace / with .
  sed 's/\//./g' | \
  
  # Replace beginning . with <package name="
  sed 's/^\./<package name="/' | \
  
  # Suffix end of line with " />
  sed 's/$/" \/>/' 
