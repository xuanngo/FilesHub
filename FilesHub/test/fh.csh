#!/bin/csh

# Absolute path to this script, e.g. /home/user/bin/foo.csh
set SCRIPT=`readlink -f "$0"`
# Absolute path this script is in, thus /home/user/bin
set SCRIPTPATH=`dirname "$SCRIPT"`

echo $SCRIPTPATH