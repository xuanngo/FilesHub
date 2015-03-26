@ECHO OFF
SET FILESHUB_HOME=%~dp0
@ECHO ON
ECHO [%*]

:: Run ..\jdk..\bin\jvisualvm.exe and then execute your program.

java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=6677 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.local.only=false -Djava.rmi.server.hostname=localhost -DFilesHub.home=%FILESHUB_HOME% -DFilesHub.hash.frequency=13 -jar %FILESHUB_HOME%fileshub.jar %*
