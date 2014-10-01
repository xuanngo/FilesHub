#!/bin/bash

# Description: Display megabytes files only.
# Author: Xuan Ngo
	
filename=$1
sed -e 's/.* KB<.*//'  $filename | \
sed -e 's/.* byte<.*//' | \
sed -e 's/.* bytes<.*//' > $filename.mb.html

sed -e 's/.* [1-9][0-9].[0-9][0-9] MB<.*//' $filename.mb.html > $filename.mb.100.html