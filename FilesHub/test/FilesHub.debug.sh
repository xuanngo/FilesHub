#!/bin/bash

# Get the path location of the executing script
## http://stackoverflow.com/questions/630372/determine-the-path-of-the-executing-bash-script
FILESHUB_HOME="`dirname \"$0\"`"                    # relative
FILESHUB_HOME="`( cd \"$FILESHUB_HOME\" && pwd )`"  # absolutized and normalized
if [ -z "$FILESHUB_HOME" ] ; then
  # error; for some reason, the path is not accessible
  # to the script (e.g. permissions re-evaled after suid)
  exit 1  # fail
fi

echo "FILESHUB_HOME=${FILESHUB_HOME}"

## ISO-8859-1
java -Dfile.encoding=UTF-8 -DFilesHub.home=${FILESHUB_HOME} -DFilesHub.debug=true -jar ${FILESHUB_HOME}/fileshub.jar "$@"
