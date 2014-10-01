#!/bin/bash

# Description: Display megabytes files only.
# Author: Xuan Ngo
# Usage: mbonly.sh inputfile outputfile
	
filename=$1
outputfilename=$2
sed 's/.* KB<.*//'  $filename | \
sed 's/.* byte<.*//' | \
sed 's/.* bytes<.*//' | \
sed -r 's/.*>[0-9][0-9]?\.?[0-9]* MB<.*//' |
sed '/^$/d' > $outputfilename