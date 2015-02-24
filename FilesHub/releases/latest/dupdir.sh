#!/bin/bash
# Description: Mark duplicate files within 2 directories.
# Author: Xuan Ngo
# Usage: 
#   dupdir.sh "/duplicate-dir-1/some-pattern*.txt" "/original-dir-2/some-pattern*.txt"
# 


# To handle filename with spaces.
####################
SAVE_IFS=$IFS
IFS=$(echo -en "\n\b")



# Pause for security.
####################
if [ "$3" = "commit" ]
then
	read -p "Are you sure?"
fi


# Main
####################
scriptname=`basename $0`


### Collect array list of files
declare -a duplicates
index=0
for duplicate in $(ls $1)
do
  duplicates[$index]=$duplicate
  index=$((index+1))
done

declare -a originals
index=0
for original in $(ls $2)
do
  originals[$index]=$original
  index=$((index+1))
done

#### Mark files as duplicates
MAX=${#originals[@]}
marked=0
for ((i=0; i<MAX; i++))
do
  echo "FilesHub.sh -d \"${duplicates[$i]}\" \"${originals[$i]}\""
  if [ "$3" = "commit" ]
  then
    FilesHub.sh -d "${duplicates[$i]}" "${originals[$i]}"
    marked=$((marked+1))
  fi  
done

green='\e[0;32m'
no_color='\e[0m'
echo -e "${green}$marked files marked as duplicate.${no_color}"

# Restore IFS
IFS=$SAVE_IFS