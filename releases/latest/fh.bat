@ECHO OFF
SET FILESHUB_HOME=%~dp0

:: ~~HELP~~
:: -DFilesHub.hash.frequency: It defines the number of times to spot hash a file when its size is greater than 4 MB.
::                            0 means hash the whole file.

:: ~~Requirements~~
::    To handle path with spaces, you have to:
::      -Change backward slash(\) to forward slash(/)
::      -Add double quotes
SET FILESHUB_HOME=%FILESHUB_HOME:\=/%

java -DFilesHub.home="%FILESHUB_HOME%" -Dlogback.configurationFile=logback.xml -DFilesHub.hash.frequency=13 -jar "%FILESHUB_HOME%Fileshub.jar" %*


:: ~~DUMP~~
:: -Dfile.encoding=UTF-8
::  ISO-8859-1