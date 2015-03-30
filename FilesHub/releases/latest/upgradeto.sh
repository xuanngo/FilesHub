#!/bin/bash

# Description: Copy the files listed over your FilesHub installation.
# Author: Xuan Ngo
# Note: To by pass the cp overwrite question you can do the following:
#			-Pipe with the 'yes' command, e.g. yes | cp XYZ.
#			or
#			-Temporarily disable alias by prefixing cp with backslash, e.g. \cp XYZ.
#		None of the method mentioned above is applied before to safeguard accidental overwrite.
	
if [ -d "$1" ]
then
  #Overwrite all files.
  yes | cp -R * "$1"
else
  echo "ERROR: $1 is not a directory." 
fi
