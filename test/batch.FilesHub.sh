#!/bin/bash

rm -f results_x_2015-03-24_*.html

MAX=20
for ((i=0; i <= MAX ; i++))  # Double parentheses, and "MAX" with no "$".
do
  rm -f FilesHub.db
  sed -i "s/frequency=.* -/frequency=${i} -/" fh
#  cp fh fh_${i}
  ./fh -a /x
done

