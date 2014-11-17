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
	# List of files to overwrite.
	cp FilesHub.bat "$1"
	cp fileshub.jar "$1"
	cp FilesHub.sh "$1"
	cp sqlite3.exe "$1"
	cp upgradeto.sh "$1"
	cp template.html "$1"
	cp words.lst "$1"
	cp -R upgrade "$1"
else
  echo "$1 is not a directory." 
fi
