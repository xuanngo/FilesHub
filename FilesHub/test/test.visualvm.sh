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

## -Dfile.encoding=UTF-8
## ISO-8859-1

##  ~~HELP~~
## -DFilesHub.hash.frequency: It defines the number of times to spot hash a file when its size is greater than 4 MB.
##                            0 means hash the whole file.

java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=6677 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.local.only=false -Djava.rmi.server.hostname=localhost -DFilesHub.home=${FILESHUB_HOME} -DFilesHub.hash.frequency=13 -jar ${FILESHUB_HOME}/Fileshub.jar "$@"
